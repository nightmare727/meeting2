package com.tiens.meeting.dubboservice.async;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.common.Result;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.imchatapi.vo.message.BatchMessageVo;
import com.tiens.meeting.repository.po.MeetingHwEventCallbackPO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingHwEventCallbackDaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

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

    @Reference(version = "1.0")
    MessageService messageService;

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
        batchMessageVo.setBody(
            JSONUtil.createObj().set("meetingRoomInfo", meetingRoomInfoPO).set("push_type", "room_info_push")
                .toString());
        /**
         * 发消息时特殊指定的行为选项,Json格式，可用于指定消息的漫游，存云端历史，发送方多端同步，推送，消息抄送等特殊行为;option中字段不填时表示默认值 option示例:
         *
         * {"push":false,"roam":true,"history":false,"sendersync":true,"route":false,"badge":false,"needPushNick":true}
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
        batchMessageVo.setPushcontent("会议消息点对点推送");
        /**
         * 必须是JSON,不能超过2k字符。该参数与APNs推送的payload含义不同
         */
//        batchMessageVo.setPayload();
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

        List<List<String>> partition = Lists.partition(toAccIds, 100);
        for (List<String> stringList : partition) {
            batchMessageVo.setToAccids(JSON.toJSONString(stringList));
            log.info("【批量发送点对点IM消息】调用入参：{}", batchMessageVo);
            Result<?> result = messageService.batchSendMessage(batchMessageVo);
            log.info("【批量发送点对点IM消息】结果返回：{}", result);
        }
    }
}
