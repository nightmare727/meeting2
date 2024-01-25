package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tiens.api.dto.MessagePayloadDTO;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.meeting.dubboservice.bo.LanguageWordBO;
import com.tiens.meeting.dubboservice.bo.PushMessageDto;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.core.LanguageService;
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
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
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

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    LanguageService languageService;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.push_message_topic}")
    String pushMessageTopic;

    @XxlJob("AppointMeetingJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog
    public void jobHandler() {

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
//            BatchAttachMessageVo batchMessageVo = new BatchAttachMessageVo();
//            batchMessageVo.setFromAccid(fromAccid);

            toAccids.add(meetingRoomInfoPO.getOwnerImUserId());

            //查询与会者集合
            toAccids.addAll(meetingAttendeeDaoService.lambdaQuery().select(MeetingAttendeePO::getAttendeeUserId)
                .eq(MeetingAttendeePO::getMeetingRoomId, meetingRoomInfoPO.getHwMeetingId()).list().stream()
                .map(MeetingAttendeePO::getAttendeeUserId).collect(Collectors.toList()));

            String languageId = meetingRoomInfoPO.getLanguageId();
            /**
             * 消息内容，最大长度 5000 字符，JSON 格式
             */

            HashMap<@Nullable String, @Nullable Object> body = Maps.newHashMap();
/*
        JSONObject roomPush = JSONUtil.createObj().set("subject", meetingRoomInfoPO.getSubject())
            .set("meetingCode", meetingRoomInfoPO.getHwMeetingCode())
            .set("startTime", DateUtil.formatDateTime(meetingRoomInfoPO.getShowStartTime()))*/
            ;
            SimpleDateFormat YMDFormat = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat HMFormat = new SimpleDateFormat("HH:mm");
            //邀请密码
            String invitePwd =
                ObjectUtil.defaultIfBlank(meetingRoomInfoPO.getGeneralPwd(), meetingRoomInfoPO.getAudiencePasswd());
            //会议时间
            String meetingTime =
                DateUtil.format(meetingRoomInfoPO.getShowStartTime(), YMDFormat) + " " + DateUtil.format(
                    meetingRoomInfoPO.getShowStartTime(), HMFormat) + "-" + DateUtil.format(
                    meetingRoomInfoPO.getShowEndTime(), HMFormat) + "(GMT+08:00)";

            JSONObject pushData = JSONUtil.createObj().set("contentImage", meetingConfig.getMeetingIcon())
                .set("contentStr",
                    languageService.getLanguageValue(languageId, meetingConfig.getMeetingStartContentKey()))
                .set("im_prefix",
                    languageService.getLanguageValue(languageId, meetingConfig.getMeetingStartPrefixContentKey()))
                .set("landingType", 2).set("landingUrl", "TencentMeetingPage").set("contentSubTitle",
                    languageService.getLanguageValue(languageId,
                        meetingConfig.getMeetingTitleKey()) + "：" + meetingRoomInfoPO.getSubject() + "\n" + languageService.getLanguageValue(
                        languageId,
                        meetingConfig.getMeetingTimeKey()) + "：" + meetingTime + "\n" + languageService.getLanguageValue(
                        languageId,
                        meetingConfig.getMeetingCodeKey()) + "：" + meetingRoomInfoPO.getHwMeetingCode() + "\n" + (
                        StrUtil.isNotBlank(invitePwd) ? "\n" + languageService.getLanguageValue(languageId,
                            meetingConfig.getMeetingPwdKey()) + "：" + invitePwd : ""));

            body.put("type", "212");
            body.put("data", pushData);
            body.put("pushTitle",
                languageService.getLanguageValue(languageId, meetingConfig.getMeetingStartPushSubTitleKey()));
            /**
             * 必须是JSON,不能超过2k字符。该参数与APNs推送的payload含义不同
             */
            MessagePayloadDTO messagePayloadDTO = new MessagePayloadDTO(body);
            ArrayList<@Nullable LanguageWordBO> languageWordBOS = Lists.newArrayList();
            //邀请您参加V-Meeting会议
            languageWordBOS.add(new LanguageWordBO(meetingConfig.getInviteTopicKey(), ""));
            //会议主题
            languageWordBOS.add(new LanguageWordBO(meetingConfig.getMeetingTitleKey(), meetingRoomInfoPO.getSubject()));
            //会议时间
            languageWordBOS.add(new LanguageWordBO(meetingConfig.getMeetingTimeKey(), meetingTime));
            //会议号
            languageWordBOS.add(
                new LanguageWordBO(meetingConfig.getMeetingCodeKey(), meetingRoomInfoPO.getHwMeetingCode()));

            PushMessageDto pushMessageDto = new PushMessageDto();
            pushMessageDto.setAccId(meetingRoomInfoPO.getOwnerImUserId());
            pushMessageDto.setTo(Lists.newArrayList(toAccids));
            pushMessageDto.setMsgType(1);
            pushMessageDto.setTitle(meetingConfig.getMeetingStartContentKey());
            pushMessageDto.setContent(languageWordBOS);
            pushMessageDto.setBody(body);
            pushMessageDto.setPayload(messagePayloadDTO);
            pushMessageDto.setPushContent(
                languageService.getLanguageValue(languageId, meetingConfig.getMeetingStartContentKey()));

            Message<String> message = MessageBuilder.withPayload(
                JSON.toJSONString(pushMessageDto, SerializerFeature.DisableCircularReferenceDetect)).build();
            log.info("【定时任务：会议开始前30分钟】【批量发送点对点IM消息】调用入参：{}", JSON.toJSONString(pushMessageDto));
            SendResult sendResult = rocketMQTemplate.syncSend(pushMessageTopic, message);
            log.info("【定时任务：会议开始前30分钟】【批量发送点对点IM消息】结果返回：{}", JSON.toJSONString(sendResult));
        }

        //修改分配资源状态及通知状态
        boolean update = meetingRoomInfoDaoService.lambdaUpdate().set(MeetingRoomInfoPO::getNotifyRoomStartStatus, 1)
            .set(MeetingRoomInfoPO::getAssignResourceStatus, 1).in(MeetingRoomInfoPO::getId, roomIds).update();

        log.info("【定时任务：会议开始前30分钟】锁定资源分配完成，roomIds:{},result:{}", roomIds, update);

    }
}
