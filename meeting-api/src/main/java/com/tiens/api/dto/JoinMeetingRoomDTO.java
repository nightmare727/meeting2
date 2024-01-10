package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/10
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class JoinMeetingRoomDTO implements Serializable {

    /**
     * accid
     */
    private String imUserId;
    /**
     * 与会者名称
     */
    private String attendeeUserName;
    /**
     * 会议号
     */
    private String meetRoomCode;
}
