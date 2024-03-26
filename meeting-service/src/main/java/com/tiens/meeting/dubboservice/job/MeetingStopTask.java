package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.impl.RpcMeetingRoomServiceImpl;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingResourceHandleEnum;
import common.enums.MeetingResourceStateEnum;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 *
 *     会议定时结束回收资源
 */
@Component
@Slf4j
public class MeetingStopTask {
    @Reference
    MessageService messageService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;
    @Autowired
    RpcMeetingRoomServiceImpl rpcMeetingRoomService;

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Autowired
    RetryTemplate retryTemplate;

    @XxlJob("MeetingStopJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog
    public void jobHandler() throws Exception {
        //1、找到结束的会议室
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));

        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
            .le(MeetingRoomInfoPO::getLockEndTime, now).list();
        if (CollectionUtil.isEmpty(list)) {
            log.info("【定时任务：会议定时结束】:当前无需要处理的数据");
            return;
        }
        log.info("【定时任务：会议定时结束】 结束的会议参数:{}", JSON.toJSONString(list));
        //会议已经结束，修改会议状态
        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            //手动结束会议
            meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getId, meetingRoomInfoPO.getId())
                .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).update();
            //释放资源状态
            rpcMeetingRoomService.publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(),
                MeetingResourceHandleEnum.HOLD_DOWN);
            //回收资源
            MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());
            if (meetingRoomInfoPO.getState().equals(MeetingRoomStateEnum.Created.getState())) {
                try {
                    hwMeetingCommonService.stopMeeting(meetingRoomInfoPO.getHwMeetingCode(),
                        meetingRoomInfoPO.getHostPwd());
                } catch (Exception e) {
                    log.error("【定时任务：会议定时结束】停止华为云会议失败，异常", e);
                    throw e;
                }
            }

            if (meetingRoomInfoPO.getOwnerImUserId()
                .equals(meetingResourcePO.getCurrentUseImUserId()) && !meetingResourcePO.getStatus()
                .equals(MeetingResourceStateEnum.PRIVATE.getState())) {
                log.info("会议自动结束回收资源，会议数据：{}", meetingRoomInfoPO);
                hwMeetingCommonService.disassociateVmr(meetingRoomInfoPO.getOwnerImUserId(),
                    Collections.singletonList(meetingResourcePO.getVmrId()));
            }

        }
        log.info("【定时任务：会议定时结束】会议定时结束完成，共执行：{}条", list.size());
    }
}
