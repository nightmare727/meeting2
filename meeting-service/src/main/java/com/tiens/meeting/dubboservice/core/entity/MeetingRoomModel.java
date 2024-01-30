package com.tiens.meeting.dubboservice.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yuwenjie
 * @TableName meeting_resouce
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MeetingRoomModel implements Serializable {

    /**
     * 华为云会议id
     */
    private String hwMeetingId;

    /**
     * 华为会议code
     */
    private String hwMeetingCode;
    /**
     *
     */
    private String state;

    /**
     * 主持人密码
     */
    private String chairmanPwd;

    /**
     * 嘉宾密码（4-16位长度的纯 数字）。
     */
    private String guestPwd;
    /**
     * 观众入会密码（网络研讨会专有）
     */
    private String audiencePasswd;

    /**
     * 与会者密码（云会议专有）
     */
    private String generalPwd;

}