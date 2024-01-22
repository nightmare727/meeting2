package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tiens.api.dto.MessagePayloadDTO;
import com.tiens.common.Result;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.imchatapi.vo.message.BatchMessageVo;
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
import java.util.HashMap;
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

    String meetingStartPrefixContent = "[会议]您参加的会议已开始";

    String inviteContentImage = "https://v-moment-prod.jikeint.com/appstatic/dazhuanpan.png";

    String meetingStartContent = "[会议]您参加的会议已开始";

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

            BatchMessageVo batchMessageVo = new BatchMessageVo();
            batchMessageVo.setFromAccid(meetingRoomInfoPO.getOwnerImUserId());
            /**
             * 0 表示文本消息,
             * 1 表示图片，
             * 2 表示语音，
             * 3 表示视频，
             * 4 表示地理位置信息，
             * 6 表示文件，
             * 10 表示提示消息，
             * 100 自定义消息类型
             */
            batchMessageVo.setType(100);
            /**
             * 消息内容，最大长度 5000 字符，JSON 格式
             */

            HashMap<@Nullable String, @Nullable Object> body = Maps.newHashMap();
/*
        JSONObject roomPush = JSONUtil.createObj().set("subject", meetingRoomInfoPO.getSubject())
            .set("meetingCode", meetingRoomInfoPO.getHwMeetingCode())
            .set("startTime", DateUtil.formatDateTime(meetingRoomInfoPO.getShowStartTime()))*/
            ;
            JSONObject pushData =
                JSONUtil.createObj().set("contentImage", inviteContentImage).set("contentStr", meetingStartContent)
                    .set("im_prefix", meetingStartPrefixContent).set("landingType", 2)
                    .set("landingUrl", "TencentMeetingPage").set("contentSubTitle",
                        "会议主题：" + meetingRoomInfoPO.getSubject() + "\n" + "会议开始时间：" + DateUtil.formatDateTime(
                            meetingRoomInfoPO.getShowStartTime()) + "\n" + "会议号：" + meetingRoomInfoPO.getHwMeetingCode());

            body.put("type", "212");
            body.put("data", pushData);
            batchMessageVo.setBody(JSON.toJSONString(body));
            /**
             * 发消息时特殊指定的行为选项,Json格式，可用于指定消息的漫游，存云端历史，发送方多端同步，推送，消息抄送等特殊行为;option中字段不填时表示默认值 option示例:
             *
             * {"push":false,"roam":true,"history":false,"sendersync":true,"route":false,"badge":false,
             * "needPushNick":true}
             *
             * 字段说明：
             * 1. roam: 该消息是否需要漫游，默认true（需要app开通漫游消息功能）；
             * 2. history: 该消息是否存云端历史，默认true；
             * 3. sendersync: 该消息是否需要发送方多端同步，默认true；
             * 4. push: 该消息是否需要APNS推送或安卓系统通知栏推送，默认true；
             * 5. route: 该消息是否需要抄送第三方；默认true (需要app开通消息抄送功能);
             * 6. badge:该消息是否需要计入到未读计数中，默认true;
             * 7. needPushNick: 推送文案是否需要带上昵称，不设置该参数时默认true;
             * 8. persistent: 是否需要存离线消息，不设置该参数时默认true。
             */
//        batchMessageVo.setOption();
            /**
             * 推送文案，最长500个字符
             */
            batchMessageVo.setPushcontent(meetingStartContent);
            /**
             * 必须是JSON,不能超过2k字符。该参数与APNs推送的payload含义不同
             */
            MessagePayloadDTO messagePayloadDTO = new MessagePayloadDTO(body);

            batchMessageVo.setPayload(JSON.toJSONString(messagePayloadDTO));
            /**
             * 开发者扩展字段，长度限制1024字符
             */
//        batchMessageVo.setExt();
            /**
             * 可选，反垃圾业务ID，实现“单条消息配置对应反垃圾”，若不填则使用原来的反垃圾配置
             */
//        batchMessageVo.setBid();
            /**
             * 可选，单条消息是否使用易盾反垃圾，可选值为0。
             * 0：（在开通易盾的情况下）不使用易盾反垃圾而是使用通用反垃圾，包括自定义消息。
             * 若不填此字段，即在默认情况下，若应用开通了易盾反垃圾功能，则使用易盾反垃圾来进行垃圾消息的判断
             */
            batchMessageVo.setUseYidun(0);
            /**
             * 可选，易盾反垃圾增强反作弊专属字段，限制json，长度限制1024字符
             */
//        batchMessageVo.setYidunAntiCheating();
            /**
             * 是否需要返回消息ID
             * false：不返回消息ID（默认值）
             * true：返回消息ID（toAccids包含的账号数量不可以超过100个）
             */
            batchMessageVo.setReturnMsgid(true);
            /**
             * 所属环境，根据env可以配置不同的抄送地址
             */
//        batchMessageVo.setEnv();

            List<List<String>> partition = Lists.partition(Lists.newArrayList(toAccids), 500);
            for (List<String> stringList : partition) {
                batchMessageVo.setToAccids(JSON.toJSONString(stringList));
                log.info("【定时任务：会议开始前30分钟】【批量发送点对点IM消息】调用入参：{}", JSON.toJSONString(batchMessageVo));
                Result<?> result = messageService.batchSendMessage(batchMessageVo);
                log.info("【定时任务：会议开始前30分钟】【批量发送点对点IM消息】结果返回：{}", JSON.toJSONString(result));
            }



            /*batchMessageVo.setToAccids(JSON.toJSONString(toAccids));
            batchMessageVo.setPushcontent(pushContent);
            batchMessageVo.setAttach(
                JSONUtil.createObj().set("pushContent", pushContent).set("push_type", "room_start_notice")
                    .set("meetingCode", meetingRoomInfoPO.getHwMeetingCode()).toString());
            //batchMessageVo.setPayload("");//不传ios收不到
            log.info("【定时任务：会议开始前30分钟】发送消息入参：{}", batchMessageVo);
            Result<?> result = messageService.batchSendAttachMessage(batchMessageVo);
            log.info("【定时任务：会议开始前30分钟】发送消息结果：{}", result);*/
            toAccids.clear();
        }

        //修改分配资源状态及通知状态
        boolean update = meetingRoomInfoDaoService.lambdaUpdate().set(MeetingRoomInfoPO::getNotifyRoomStartStatus, 1)
            .set(MeetingRoomInfoPO::getAssignResourceStatus, 1).in(MeetingRoomInfoPO::getId, roomIds).update();

        log.info("【定时任务：会议开始前30分钟】锁定资源分配完成，roomIds:{},result:{}", roomIds, update);

    }
}
