package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.common.Result;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.imchatapi.vo.message.BatchAttachMessageVo;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 *
 *     会议30分钟前发送消息
 */
@Component
@Slf4j
public class MeetingStopNoticeTask {
    @Reference(version = "1.0")
    MessageService messageService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    RetryTemplate retryTemplate;

    @Value("${live.fromAccid}")
    String fromAccid;
    @Value("${live.stopPushContent}")
    String pushContent;

    @XxlJob("MeetingStopNoticeJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog
    public void jobHandler() throws Exception {
        //1、找到快结束的会议室，给主持人发送IM消息

        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Created.getState())
            .eq(MeetingRoomInfoPO::getNotifyRoomStopStatus, 0).le(MeetingRoomInfoPO::getShowEndTime, DateUtil.date())
            .list();
//            .le(MeetingRoomInfoPO::getLockEndTime, DateUtil.offsetMinute(new Date(), 90)).list();
        if (CollectionUtil.isEmpty(list)) {
            log.info("【定时任务：会议结束前30分钟】:当前无需要通知的消息");
            return;
        }

        List<String> toAccIds =
            list.stream().map(MeetingRoomInfoPO::getOwnerImUserId).distinct().collect(Collectors.toList());

        List<Long> ids = list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList());

        BatchAttachMessageVo batchMessageVo = new BatchAttachMessageVo();
        batchMessageVo.setFromAccid(fromAccid);
        batchMessageVo.setToAccids(JSON.toJSONString(toAccIds));
        batchMessageVo.setPushcontent(pushContent);
        batchMessageVo.setAttach(
            JSONUtil.createObj().set("pushContent", pushContent).set("push_type", "room_stop_notice").toString());
//        batchMessageVo.setPayload("");//不传ios收不到
        log.info("【定时任务：会议结束前30分钟】发送消息入参：{}", batchMessageVo);
        Result<?> result = messageService.batchSendAttachMessage(batchMessageVo);
        log.info("【定时任务：会议结束前30分钟】发送消息结果：{}", result);

        meetingRoomInfoDaoService.lambdaUpdate().set(MeetingRoomInfoPO::getNotifyRoomStopStatus, 1)
            .in(MeetingRoomInfoPO::getId, ids).update();
        log.info("【定时任务：会议结束前30分钟】发送消息完成，共执行：{}条", ids.size());
    }
}
