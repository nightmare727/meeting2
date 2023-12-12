package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 预约提前30分钟锁定资源
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

    @XxlJob("AppointMeetingJobHandler")
    public void jobHandler() throws Exception {
        //1、预约提前30分钟锁定资源
        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState())
            .ge(MeetingRoomInfoPO::getLockStartTime, DateUtil.date()).eq(MeetingRoomInfoPO::getAssignResourceStatus, 0)
            .list();
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

        log.info("会议开始前30分钟前锁定资源分配完成】，id:{},result:{}", roomIds, update);

    }
}
