package com.tiens.meeting.dubboservice.async;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tiens.api.dto.MessagePayloadDTO;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.meeting.dubboservice.bo.LanguageWordBO;
import com.tiens.meeting.dubboservice.bo.PushMessageDto;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.LanguageService;
import com.tiens.meeting.repository.po.MeetingHwEventCallbackPO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingHwEventCallbackDaoService;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoomAsyncTask implements RoomAsyncTaskService {

    private final MeetingHwEventCallbackDaoService meetingHwEventCallbackDaoService;

//    @Reference(version = "1.0")
//    MessageService messageService;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.producer.push_message_topic}")
    String pushMessageTopic;

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    LanguageService languageService;

    /**
     * 保存回调记录
     *
     * @param hwEventReq
     */
    @Override
    public void saveHwEventLog(HwEventReq hwEventReq) {
        if (!hwEventReq.getRetryFlag()) {
            EventInfo eventInfo = hwEventReq.getEventInfo();
            Payload payload = eventInfo.getPayload();
            MeetingHwEventCallbackPO meetingHwEventCallbackPO = new MeetingHwEventCallbackPO();
            meetingHwEventCallbackPO.setAppId(hwEventReq.getAppID());
            meetingHwEventCallbackPO.setTimestamp(DateUtil.date(hwEventReq.getTimestamp()));
            meetingHwEventCallbackPO.setEvent(eventInfo.getEvent());
            meetingHwEventCallbackPO.setPayload(JSON.toJSONString(payload));
            meetingHwEventCallbackPO.setMeetingCode(payload.getMeetingInfo().getMeetingID());
            meetingHwEventCallbackPO.setMeetingId(payload.getMeetingInfo().getMeetingUUID());

            meetingHwEventCallbackDaoService.save(meetingHwEventCallbackPO);
        }

    }

    /**
     * 批量发送点对点IM消息
     *
     * @param meetingRoomInfoPO
     * @param toAccIds
     */
    @Override
    public void batchSendIMMessage(MeetingRoomInfoPO meetingRoomInfoPO, List<String> toAccIds) {
        log.info("【批量发送点对点IM消息】 会议入参：{}，接收人：{}", meetingRoomInfoPO, toAccIds);

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
        String timeZoneOffset = meetingRoomInfoPO.getTimeZoneOffset();

        String meetingTime = DateUtil.format(
            DateUtils.convertTimeZone(meetingRoomInfoPO.getShowStartTime(), DateUtils.TIME_ZONE_GMT,
                ZoneId.of(timeZoneOffset)), YMDFormat) + " " + DateUtil.format(
            DateUtils.convertTimeZone(meetingRoomInfoPO.getShowStartTime(), DateUtils.TIME_ZONE_GMT,
                ZoneId.of(timeZoneOffset)), HMFormat) + "-" + DateUtil.format(
            DateUtils.convertTimeZone(meetingRoomInfoPO.getShowEndTime(), DateUtils.TIME_ZONE_GMT,
                ZoneId.of(timeZoneOffset)), HMFormat) + "(" + timeZoneOffset + ")";

        JSONObject pushData = JSONUtil.createObj().set("contentImage", meetingConfig.getMeetingIcon())
            .set("contentStr", languageService.getLanguageValue(languageId, meetingConfig.getInviteContentKey()))
            .set("im_prefix", languageService.getLanguageValue(languageId, meetingConfig.getInviteImPrefixContentKey()))
            .set("landingType", 2).set("landingUrl", "TencentMeetingPage").set("contentSubTitle",
                languageService.getLanguageValue(languageId,
                    meetingConfig.getMeetingTitleKey()) + "：" + meetingRoomInfoPO.getSubject() + "\n" + languageService.getLanguageValue(
                    languageId,
                    meetingConfig.getMeetingTimeKey()) + "：" + meetingTime + "\n" + languageService.getLanguageValue(
                    languageId, meetingConfig.getMeetingCodeKey()) + "：" + meetingRoomInfoPO.getHwMeetingCode() + (
                    StrUtil.isNotBlank(invitePwd) ? "\n" + languageService.getLanguageValue(languageId,
                        meetingConfig.getMeetingPwdKey()) + "：" + invitePwd : ""));

        body.put("type", "212");
        body.put("data", pushData);
        body.put("pushTitle",
            languageService.getLanguageValue(languageId, meetingConfig.getMeetingInvitePushSubTitleKey()));
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
        pushMessageDto.setTo(toAccIds);
        pushMessageDto.setMsgType(1);
        pushMessageDto.setTitle(meetingConfig.getInviteContentKey());
        pushMessageDto.setContent(languageWordBOS);
        pushMessageDto.setBody(body);
        pushMessageDto.setPayload(messagePayloadDTO);
        pushMessageDto.setPushContent(
            languageService.getLanguageValue(languageId, meetingConfig.getInviteContentKey()));
        pushMessageDto.setSubtype(1);
        Message<String> message = MessageBuilder.withPayload(
            JSON.toJSONString(pushMessageDto, SerializerFeature.DisableCircularReferenceDetect)).build();
        log.info("【批量发送点对点IM消息】调用入参：{}", JSON.toJSONString(pushMessageDto));
        SendResult sendResult = rocketMQTemplate.syncSend(pushMessageTopic, message);
        log.info("【批量发送点对点IM消息】结果返回：{}", JSON.toJSONString(sendResult));

    }

}
