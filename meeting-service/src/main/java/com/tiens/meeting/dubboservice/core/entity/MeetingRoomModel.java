package com.tiens.meeting.dubboservice.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yuwenjie
 * @TableName meeting_resouce
 */
@Data
@AllArgsConstructor
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

}