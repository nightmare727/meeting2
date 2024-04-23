package com.tiens.meeting.dubboservice.async;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nacos.common.http.param.MediaType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tiens.api.dto.MessagePayloadDTO;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.meeting.dubboservice.bo.LanguageWordBO;
import com.tiens.meeting.dubboservice.bo.PushMessageDto;
import com.tiens.meeting.dubboservice.common.entity.SyncCommonResult;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.LanguageService;
import com.tiens.meeting.dubboservice.model.UserExpAddEntity;
import com.tiens.meeting.repository.po.*;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingHwEventCallbackDaoService;
import com.tiens.meeting.repository.service.MeetingMultiPersonAwardDaoService;
import com.tiens.meeting.repository.service.MeetingMultiPersonAwardRecordDaoService;
import com.tiens.meeting.util.VmUserUtil;
import common.enums.MeetingUserJoinStatusEnum;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    MeetingMultiPersonAwardDaoService multiPersonAwardDaoService;

    @Autowired
    MeetingMultiPersonAwardRecordDaoService meetingMultiPersonAwardRecordDaoService;

    @Autowired
    MeetingAttendeeDaoService meetingAttendeeDaoService;

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

    /**
     * 发放多人会议奖励
     *
     * @param meetingRoomInfoPO
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void doSendMultiPersonsAward(MeetingRoomInfoPO meetingRoomInfoPO) {
        String ownerImUserId = meetingRoomInfoPO.getOwnerImUserId();

        //发放经验集合
        LinkedHashMap<Integer, UserExpAddEntity> userExpAddEntityMap = Maps.newLinkedHashMap();

        //发放日志集合
        ArrayList<MeetingMultiPersonAwardRecordPO> meetingMultiPersonAwardRecordPOS = Lists.newArrayList();
        //发放统计集合
        ArrayList<MeetingMultiPersonAwardPO> meetingMultiPersonAwardPOS = Lists.newArrayList();

        List<MeetingConfig.MultiPersonsAwardInner> multiPersonsAwardConfig = meetingConfig.getMultiPersonsAwardConfig();

        Map<Integer, MeetingConfig.MultiPersonsAwardInner> configMap = multiPersonsAwardConfig.stream()
            .collect(Collectors.toMap(MeetingConfig.MultiPersonsAwardInner::getPersonNum, Function.identity()));

        //3、判断剩余可发放奖励

        Map<Integer, Integer> remainMultiPersonsAward =
            getRemainMultiPersonsAward(ownerImUserId, multiPersonsAwardConfig);

        Map<Integer, Integer> filterMap = remainMultiPersonsAward.entrySet().stream().filter(t -> t.getValue() > 0)
            .collect(Collectors.toMap(Map.Entry<Integer, Integer>::getKey, Map.Entry<Integer, Integer>::getValue));

       /* TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();

        for (int i = 0; i < multiPersonsAwardConfig.size(); i++) {
            MeetingConfig.MultiPersonsAwardInner curMultiPersonsAwardInner = multiPersonsAwardConfig.get(i);
            Integer cuPersonNum = curMultiPersonsAwardInner.getPersonNum();
            Integer nextPersonNum = i == multiPersonsAwardConfig.size() ? Integer.MAX_VALUE
                : multiPersonsAwardConfig.get(i + 1).getPersonNum();
            rangeSet.add(Range.closedOpen(cuPersonNum, nextPersonNum));
        }*/
        if (CollectionUtil.isNotEmpty(filterMap)) {
            //查询本场会议得人数
            Long count = meetingAttendeeDaoService.lambdaQuery()
                .eq(MeetingAttendeePO::getMeetingRoomId, meetingRoomInfoPO.getId())
                .eq(MeetingAttendeePO::getJoinStatus, MeetingUserJoinStatusEnum.JOINED.getCode()).count();

            filterMap.forEach((k, v) -> {
                if (count >= k) {
                    //满足该条件
                    MeetingConfig.MultiPersonsAwardInner multiPersonsAwardInner = configMap.get(k);

                    //构建发送经验实体
                    UserExpAddEntity userExpAddEntity = new UserExpAddEntity();
                    userExpAddEntity.setExperience(multiPersonsAwardInner.getAwardValue());
                    userExpAddEntity.setOperateType(1);
                    userExpAddEntity.setCoinSource(multiPersonsAwardInner.getCoinSource());
                    userExpAddEntityMap.put(k, userExpAddEntity);

                    //构建会议多人奖励日志实体
                    MeetingMultiPersonAwardRecordPO meetingMultiPersonAwardRecordPO =
                        new MeetingMultiPersonAwardRecordPO();
                    meetingMultiPersonAwardRecordPO.setMeetingId(meetingRoomInfoPO.getId());
                    meetingMultiPersonAwardRecordPO.setMeetingCode(meetingRoomInfoPO.getHwMeetingCode());
                    meetingMultiPersonAwardRecordPO.setMeetingRelPersonCount(Math.toIntExact(count));
                    meetingMultiPersonAwardRecordPO.setAwardCount(k);
                    meetingMultiPersonAwardRecordPO.setImUserId(ownerImUserId);
                    meetingMultiPersonAwardRecordPOS.add(meetingMultiPersonAwardRecordPO);

                    //构建会议多人奖励记录统计实体
                    MeetingMultiPersonAwardPO meetingMultiPersonAwardPO = new MeetingMultiPersonAwardPO();
                    meetingMultiPersonAwardPO.setAwardCount(1);
                    meetingMultiPersonAwardPO.setImUserId(ownerImUserId);
                    meetingMultiPersonAwardPO.setAwardSize(multiPersonsAwardInner.getPersonNum());
                    meetingMultiPersonAwardPO.setRemark(
                        String.format("来自会议号:%s 新增一次经验奖励", meetingRoomInfoPO.getHwMeetingCode()));
                    meetingMultiPersonAwardPOS.add(meetingMultiPersonAwardPO);
                }
            });
        }

        //4、保存发放数据
        //4.1保存发放日志记录
        meetingMultiPersonAwardRecordDaoService.saveBatch(meetingMultiPersonAwardRecordPOS);

        //4.2保存发放统计
        for (MeetingMultiPersonAwardPO meetingMultiPersonAwardPO : meetingMultiPersonAwardPOS) {
            try {
                multiPersonAwardDaoService.save(meetingMultiPersonAwardPO);
            } catch (DuplicateKeyException e) {
                //数据重复执行更新操作
                multiPersonAwardDaoService.lambdaUpdate().eq(MeetingMultiPersonAwardPO::getImUserId, ownerImUserId)
                    .eq(MeetingMultiPersonAwardPO::getAwardSize, meetingMultiPersonAwardPO.getAwardSize())
                    .set(MeetingMultiPersonAwardPO::getRemark, meetingMultiPersonAwardPO.getRemark())
                    .setSql("award_count=award_count+1").update();
            }
        }

        //5、调用经验发放接口
        userExpAddEntityMap.forEach((k, v) -> {
            Map<String, String> authHead = VmUserUtil.getAuthHead(ownerImUserId);
            String param = JSON.toJSONString(v);
            Boolean syncResult = false;
            try {
                HttpResponse execute =
                    HttpUtil.createPost(meetingConfig.getMultiPersonsAwardSyncUrl()).addHeaders(authHead)
                        .body(param, MediaType.APPLICATION_JSON).execute();
                String result = execute.body();
                SyncCommonResult syncCommonResult = JSON.parseObject(result, SyncCommonResult.class);
                if (syncCommonResult.getSuccess()) {
                    log.info("同步会议多人经验奖励成功请求头：{}，参数：{}，返回结果：{}", authHead, param, result);
                    syncResult = true;
                } else {
                    log.error("同步会议多人经验奖励失败，请求头：{}，参数：{}，返回结果：{}", authHead, param, result);
                }
            } catch (Exception e) {
                log.error("同步会议多人经验奖励异常，");
            }

            //修改同步结果
            meetingMultiPersonAwardRecordDaoService.lambdaUpdate()
                .eq(MeetingMultiPersonAwardRecordPO::getImUserId, ownerImUserId)
                .eq(MeetingMultiPersonAwardRecordPO::getAwardCount, k)
                .set(MeetingMultiPersonAwardRecordPO::getSyncResult, syncResult ? 1 : 2).update();
        });

    }

    /**
     * 获取某主持人剩余多人会议奖励数
     *
     * @param imUserId
     * @param multiPersonsAwardConfig
     * @return map 【（100,1）,(200,2),(500,5)】
     */
    public Map<Integer, Integer> getRemainMultiPersonsAward(String imUserId,
        List<MeetingConfig.MultiPersonsAwardInner> multiPersonsAwardConfig) {

        LinkedHashMap<Integer, Integer> result = Maps.newLinkedHashMap();

        //2、查询已发放奖励数组
        Map<Integer, List<MeetingMultiPersonAwardPO>> awardSizeMap =
            multiPersonAwardDaoService.lambdaQuery().eq(MeetingMultiPersonAwardPO::getImUserId, imUserId)
                .orderByAsc(MeetingMultiPersonAwardPO::getAwardSize).list().stream()
                .collect(Collectors.groupingBy(MeetingMultiPersonAwardPO::getAwardSize));

        for (MeetingConfig.MultiPersonsAwardInner multiPersonsAwardInner : multiPersonsAwardConfig) {
            Integer personNum = multiPersonsAwardInner.getPersonNum();
            Integer count = multiPersonsAwardInner.getCount();
            List<MeetingMultiPersonAwardPO> meetingMultiPersonAwardPOS = awardSizeMap.get(personNum);
            if (ObjectUtil.isNotEmpty(meetingMultiPersonAwardPOS)) {
                MeetingMultiPersonAwardPO meetingMultiPersonAwardPO = meetingMultiPersonAwardPOS.get(0);
                Integer awardCount = meetingMultiPersonAwardPO.getAwardCount();
                //获取目前已累加次数，减去获得剩余次数
                count = count - awardCount;
            }

            result.put(personNum, count);
        }
        return result;
    }
}
