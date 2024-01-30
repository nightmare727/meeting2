package com.tiens.meeting.dubboservice.async;

import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
public interface RoomAsyncTaskService {
    /**
     * 保存回调记录
     *
     * @param hwEventReq
     */
    @Async
    void saveHwEventLog(HwEventReq hwEventReq);

    /**
     * 批量发送点对点IM消息
     *
     * @param meetingRoomInfoPO
     * @param toAccIds
     */
    void batchSendIMMessage(MeetingRoomInfoPO meetingRoomInfoPO, List<String> toAccIds);

}
