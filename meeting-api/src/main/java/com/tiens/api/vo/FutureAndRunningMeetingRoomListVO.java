package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/8
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class FutureAndRunningMeetingRoomListVO implements Serializable {
    /**
     * key是yyyy-MM-dd格式日为维度的时间，value 是会议列表
     */
    private Map<String, List<MeetingRoomDetailDTO>> rooms;

}
