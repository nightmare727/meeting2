package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.tiens.common.Result;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.imchatapi.vo.message.BatchAttachMessageVo;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingResourceStateEnum;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    @Reference(version = "1.0")
    MessageService messageService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;
    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Autowired
    MeetingAttendeeDaoService meetingAttendeeDaoService;

    @Value("${live.fromAccid}")
    String fromAccid;
    @Value("${live.startPushContent}")
    String pushContent;

    @XxlJob("AppointMeetingJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog
    public void jobHandler() throws Exception {

        //1、预约提前30分钟锁定资源
        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState())
            .eq(MeetingRoomInfoPO::getNotifyRoomStartStatus, 0).le(MeetingRoomInfoPO::getLockStartTime, DateUtil.date())
            .list();
        if (CollectionUtil.isEmpty(list)) {
            log.info("【定时任务：会议开始前30分钟】 当前无需要通知的消息");
            return;
        }

        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            String ownerImUserId = meetingRoomInfoPO.getOwnerImUserId();
            MeetingResourcePO byId = meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());
            if (!byId.getStatus().equals(MeetingResourceStateEnum.PRIVATE.getState())) {
                log.info("【定时任务：会议开始前30分钟】 执行分配资源，ownerImUserId：{},vmrId:{}", ownerImUserId,
                    byId.getVmrId());
                hwMeetingCommonService.associateVmr(ownerImUserId, Collections.singletonList(byId.getVmrId()));
            }
        }

        List<Long> roomIds = list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList());
        Set<@Nullable String> toAccids = Sets.newHashSet();
        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            BatchAttachMessageVo batchMessageVo = new BatchAttachMessageVo();
            batchMessageVo.setFromAccid(fromAccid);

            toAccids.add(meetingRoomInfoPO.getOwnerImUserId());
            //查询与会者集合
            toAccids.addAll(meetingAttendeeDaoService.lambdaQuery().select(MeetingAttendeePO::getAttendeeUserId)
                .eq(MeetingAttendeePO::getMeetingRoomId, meetingRoomInfoPO.getHwMeetingId()).list().stream()
                .map(MeetingAttendeePO::getAttendeeUserId).collect(Collectors.toList()));

            batchMessageVo.setToAccids(JSON.toJSONString(toAccids));
            batchMessageVo.setPushcontent(pushContent);
            batchMessageVo.setAttach(
                JSONUtil.createObj().set("pushContent", pushContent).set("push_type", "room_start_notice")
                    .set("meetingCode", meetingRoomInfoPO.getHwMeetingCode()).toString());
//        batchMessageVo.setPayload("");//不传ios收不到
            log.info("【定时任务：会议开始前30分钟】发送消息入参：{}", batchMessageVo);
            Result<?> result = messageService.batchSendAttachMessage(batchMessageVo);
            log.info("【定时任务：会议开始前30分钟】发送消息结果：{}", result);
            toAccids.clear();
        }

        //修改分配资源状态及通知状态
        boolean update = meetingRoomInfoDaoService.lambdaUpdate().set(MeetingRoomInfoPO::getNotifyRoomStartStatus, 1)
            .set(MeetingRoomInfoPO::getAssignResourceStatus, 1).in(MeetingRoomInfoPO::getId, roomIds).update();

        log.info("【定时任务：会议开始前30分钟】锁定资源分配完成，roomIds:{},result:{}", roomIds, update);

    }
}
