package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/12
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class CancelMeetingRoomDTO implements Serializable {

    private Long meetingRoomId;

    private String imUserId;
}
