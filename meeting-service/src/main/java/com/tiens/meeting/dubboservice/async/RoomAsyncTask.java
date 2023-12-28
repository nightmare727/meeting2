package com.tiens.meeting.dubboservice.async;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.meeting.repository.po.MeetingHwEventCallbackPO;
import com.tiens.meeting.repository.service.MeetingHwEventCallbackDaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
@Service
@RequiredArgsConstructor
public class RoomAsyncTask implements RoomAsyncTaskService {

    private final MeetingHwEventCallbackDaoService meetingHwEventCallbackDaoService;

    /**
     * 保存回调记录
     *
     * @param hwEventReq
     */
    @Override
    public void saveHwEventLog(HwEventReq hwEventReq) {
        if(!hwEventReq.getRetryFlag()){
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
}
