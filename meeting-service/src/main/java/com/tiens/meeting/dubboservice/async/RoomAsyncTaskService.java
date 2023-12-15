package com.tiens.meeting.dubboservice.async;

import com.tiens.api.dto.hwevent.HwEventReq;
import org.springframework.scheduling.annotation.Async;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
@Async
public interface RoomAsyncTaskService {
    /**
     * 保存回调记录
     *
     * @param hwEventReq
     */
    void saveHwEventLog(HwEventReq hwEventReq);
}
