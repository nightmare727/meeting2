package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName meeting_room_info
 */
@TableName(value = "meeting_room_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingRoomInfoPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 华为云会议id
     */
    @TableField(value = "hw_meeting_id", updateStrategy = FieldStrategy.IGNORED)
    private String hwMeetingId;

    /**
     * 华为会议code
     */
    @TableField(value = "hw_meeting_code", updateStrategy = FieldStrategy.IGNORED)
    private String hwMeetingCode;

    /**
     * 会议开始时间
     */
    @TableField(value = "show_start_time")
    private Date showStartTime;

    /**
     * 会议结束时间
     */
    @TableField(value = "show_end_time")
    private Date showEndTime;

    /**
     * 锁定开始时间
     */
    @TableField(value = "lock_start_time")
    private Date lockStartTime;

    /**
     * 锁定结束时间
     */
    @TableField(value = "lock_end_time")
    private Date lockEndTime;

    /**
     * 会议真实开始时间
     */
    @TableField(value = "rel_start_time")
    private Date relStartTime;

    /**
     * 会议真实结束时间
     */
    @TableField(value = "rel_end_time")
    private Date relEndTime;

    /**
     * 会议通知中会议时间的时区信息
     */
    @TableField(value = "time_zone_id")
    private Integer timeZoneId;

    /**
     * 时区偏移量
     */
    @TableField(value = "time_zone_offset")
    private String timeZoneOffset;

    /**
     * 会议持续时长，单位分钟。默认30分钟。最大1440分钟（24小时），最小15分钟。
     */
    @TableField(value = "duration")
    private Integer duration;

    /**
     * 资源id
     */
    @TableField(value = "resource_id")
    private Integer resourceId;

    /**
     * 主持人accid
     */
    @TableField(value = "owner_im_user_id")
    private String ownerImUserId;
    /**
     * 主持人昵称
     */
    @TableField(value = "owner_user_name")
    private String ownerUserName;
    /**
     * 会议状态。 ● “Schedule”：预定状 态 ● “Creating”：正在创 建状态 ● “Created”：会议已 经被创建，并正在召开 ● “Destroyed”：会议 已经关闭
     */
    @TableField(value = "state")
    private String state;
    /**
     * 云会议类型 1：云会议室  2：网络研讨会
     */
    @TableField(value = "vmr_mode")
    private Integer vmrMode;
    /**
     * 录制状态 0：未录制完成  1：录制完成
     */
    @TableField(value = "record_status")
    private Integer recordStatus;

    /**
     * 分配状态 0：未分配 1：已分配
     */
    @TableField(value = "assign_resource_status")
    private Integer assignResourceStatus;
    /**
     * 会议结束通知状态 0：未通知 1：已通知
     */
    @TableField(value = "notify_room_stop_status")
    private Integer notifyRoomStopStatus;

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

    /**
     * 是否删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;
    /**
     * 会议标题
     */
    @TableField(value = "subject")
    private String subject;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}