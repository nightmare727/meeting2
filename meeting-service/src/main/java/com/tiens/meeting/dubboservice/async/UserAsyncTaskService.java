package com.tiens.meeting.dubboservice.async;

import com.tiens.china.circle.api.dto.HomepageUserDTO;
import org.springframework.scheduling.annotation.Async;

/**
 * @Author: 蔚文杰
 * @Date: 2024/4/11
 * @Version 1.0
 * @Company: tiens
 */
public interface UserAsyncTaskService {
    /**
     * 同步修改直播主播数据
     *
     * @param homepageUserDTO
     */
    @Async
    void updateLiveAnchorInfo(HomepageUserDTO homepageUserDTO);
}
