package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 会议与会者表
 *
 * @TableName meeting_attendee
 */
@TableName(value = "meeting_attendee")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingAttendeePO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会议id
     */
    @TableField(value = "meeting_room_id")
    private Long meetingRoomId;

    /**
     * 邀请人的id
     */
    @TableField(value = "attendee_user_id")
    private String attendeeUserId;

    /**
     * 邀请人名称
     */
    @TableField(value = "attendee_user_name")
    private String attendeeUserName;
    /**
     * 邀请人头像
     */
    @TableField(value = "attendee_user_head_url")
    private String attendeeUserHeadUrl;
    /**
     * 来源 1：预约 2：中途加入
     */
    @TableField(value = "source")
    private Integer source;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingAttendeePO that = (MeetingAttendeePO)o;
        return meetingRoomId.equals(that.meetingRoomId) && attendeeUserId.equals(that.attendeeUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meetingRoomId, attendeeUserId);
    }
}