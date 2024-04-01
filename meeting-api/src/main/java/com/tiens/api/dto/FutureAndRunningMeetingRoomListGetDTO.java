package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/3/27
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class FutureAndRunningMeetingRoomListGetDTO implements Serializable {
    /**
     * accid
     */
    private String finalUserId;
    /**
     * 时区
     */
    private String timeZoneOffset;
}
