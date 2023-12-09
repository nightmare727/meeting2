package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.tiens.common.Result;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.imchatapi.vo.message.BatchAttachMessageVo;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
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
public class MeetingStopTask {
    @Reference
    MessageService messageService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    RetryTemplate retryTemplate;

    @Value("${live.fromAccid}")
    String fromAccid;
    @Value("${live.pushcontent}")
    String pushContent;

    @XxlJob("MeetingStopJobHandler")
    public void jobHandler() throws Exception {
        //1、找到快结束的会议室，给主持人发送IM消息

        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Created.getState())
            .eq(MeetingRoomInfoPO::getNotifyRoomStopStatus, 0)
            .le(MeetingRoomInfoPO::getLockEndTime, DateUtil.offsetMinute(new Date(), 30)).list();
        if (CollectionUtil.isEmpty(list)) {
            log.info("【会议结束前30分钟前发送消息】:当前无需要通知的消息");
            return;
        }

        String toAccIds =
            list.stream().map(MeetingRoomInfoPO::getOwnerImUserId).distinct().collect(Collectors.joining(","));

        List<Long> ids = list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList());

        BatchAttachMessageVo batchMessageVo = new BatchAttachMessageVo();
        batchMessageVo.setFromAccid(fromAccid);
        batchMessageVo.setToAccids(toAccIds);
        batchMessageVo.setPushcontent(pushContent);
        batchMessageVo.setAttach(JSONUtil.createObj().putOnce("pushContent", pushContent).toString());
//        batchMessageVo.setPayload("");//不传ios收不到
        Result<?> result = messageService.batchSendAttachMessage(batchMessageVo);
        log.info("定时推送会议30分钟前发送消息结果：{}", result);

        meetingRoomInfoDaoService.lambdaUpdate().set(MeetingRoomInfoPO::getNotifyRoomStopStatus, 1)
            .in(MeetingRoomInfoPO::getId, ids).update();
        log.info("会议结束前30分钟前发送消息完成，共执行：{}条", ids.size());
    }
}
