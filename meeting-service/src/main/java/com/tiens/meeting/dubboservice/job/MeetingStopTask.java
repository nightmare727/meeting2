package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingRoomStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

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

    @XxlJob("MeetingStopJobHandler")
    public void jobHandler() throws Exception {
        //1、找到快结束的会议室，给主持人发送IM消息

        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Created.getState())
            .le(MeetingRoomInfoPO::getLockEndTime, DateUtil.offsetMinute(new Date(), 30)).list();
        if (CollectionUtil.isEmpty(list)) {
            log.info("【会议30分钟前发送消息】:当前无需要通知的消息");
            return;
        }
//        messageService.batchSendMessage()
    }
}
