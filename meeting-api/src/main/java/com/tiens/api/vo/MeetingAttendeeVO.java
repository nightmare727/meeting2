package com.tiens.api.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 会议与会者表
 *
 * @TableName meeting_attendee
 */
@Data
public class MeetingAttendeeVO implements Serializable {

    /**
     * 会议id
     */
    private Long meetingRoomId;

    /**
     * 邀请人的id
     */
    private String attendeeUserId;

    /**
     * 邀请人名称
     */
    private String attendeeUserName;

    /**
     * 来源 1：预约 2：中途加入
     */
    @TableField(value = "source")
    private Integer source;

}