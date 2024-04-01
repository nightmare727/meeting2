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
public class HistoryMeetingRoomListGetDTO implements Serializable {
    /**
     * 时区
     */
    String timeZoneOffset;
    /**
     * 月份
     */
    private Integer month;
    /**
     * accid
     */
    private String finalUserId;

}
