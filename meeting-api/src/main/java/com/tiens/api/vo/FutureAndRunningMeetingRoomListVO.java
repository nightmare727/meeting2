package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/8
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class FutureAndRunningMeetingRoomListVO implements Serializable {
    /**
     * 今天会议集合
     */
    private List<MeetingRoomDetailDTO> todayRooms;
    /**
     * 明天会议集合
     */
    private List<MeetingRoomDetailDTO> tomorrowRooms;
    /**
     * 其他日期集合
     */
    private List<MeetingRoomDetailDTO> otherRooms;
}
