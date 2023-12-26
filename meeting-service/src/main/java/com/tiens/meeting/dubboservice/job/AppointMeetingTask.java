package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.common.Result;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.imchatapi.vo.message.BatchAttachMessageVo;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 公有预约提前30分钟锁定资源
 */
@Component
@Slf4j
public class AppointMeetingTask {

    @Reference
    MessageService messageService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;
    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Value("${live.fromAccid}")
    String fromAccid;
    @Value("${live.startPushContent}")
    String pushContent;

    @XxlJob("AppointMeetingJobHandler")
    @Transactional(rollbackFor = Exception.class)
    public void jobHandler() throws Exception {

        //1、预约提前30分钟锁定资源
        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState())
            .eq(MeetingRoomInfoPO::getNotifyRoomStartStatus, 0).le(MeetingRoomInfoPO::getLockStartTime, DateUtil.date())
            .eq(MeetingRoomInfoPO::getAssignResourceStatus, 0).list();
        if (CollectionUtil.isEmpty(list)) {
            log.info("【会议开始前30分钟前发送消息】:当前无需要通知的消息");
            return;
        }
        List<Long> roomIds = list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList());

        boolean update = meetingRoomInfoDaoService.lambdaUpdate().in(MeetingRoomInfoPO::getId, roomIds)
            .set(MeetingRoomInfoPO::getAssignResourceStatus, 1).update();
        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            String ownerImUserId = meetingRoomInfoPO.getOwnerImUserId();
            MeetingResourcePO byId = meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());
            hwMeetingCommonService.associateVmr(ownerImUserId, Collections.singletonList(byId.getVmrId()));
        }

        List<Long> ids = list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList());

        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            BatchAttachMessageVo batchMessageVo = new BatchAttachMessageVo();
            batchMessageVo.setFromAccid(fromAccid);
            batchMessageVo.setToAccids(JSON.toJSONString(Collections.singletonList(meetingRoomInfoPO.getOwnerImUserId())));
            batchMessageVo.setPushcontent(pushContent);
            batchMessageVo.setAttach(
                JSONUtil.createObj().set("pushContent", pushContent)
                    .set("push_type", "room_start_notice")
                    .set("meetingCode", meetingRoomInfoPO.getHwMeetingCode())
                    .toString());
//        batchMessageVo.setPayload("");//不传ios收不到
            log.info("【会议开始前30分钟前发送消息】发送消息入参：{}", batchMessageVo);
            Result<?> result = messageService.batchSendAttachMessage(batchMessageVo);
            log.info("【会议开始前30分钟前发送消息】发送消息结果：{}", result);
        }
        meetingRoomInfoDaoService.lambdaUpdate().set(MeetingRoomInfoPO::getNotifyRoomStartStatus, 1)
            .in(MeetingRoomInfoPO::getId, ids).update();

        log.info("会议开始前30分钟前锁定资源分配完成】，id:{},result:{}", roomIds, update);

    }
}
