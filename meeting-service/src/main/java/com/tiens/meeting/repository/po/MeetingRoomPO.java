package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @TableName meeting_room
 */
@TableName(value ="meeting_room")
@Data
@EqualsAndHashCode
public class MeetingRoomPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会议ID
     */
    @TableField(value = "meeting_id")
    private String meetingId;

    /**
     * 会议code
     */
    @TableField(value = "meeting_code")
    private String meetingCode;

    /**
     * 会议主题
     */
    @TableField(value = "subject")
    private String subject;

    /**
     * 会议类型(0-一次性会议，1-周期性会议，2-微信专属会议，4-rooms投屏会议，5-个人会议号会议)
     */
    @TableField(value = "meeting_type")
    private Integer meetingType;

    /**
     * 秒级别的会议开始时间戳
     */
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 秒级别的会议结束时间戳
     */
    @TableField(value = "end_time")
    private Date endTime;

    /**
     * 录制状态 1：录制中 2：转码中 3：转码完成
     */
    @TableField(value = "record_status")
    private Integer recordStatus;

    /**
     * 会议状态 1：待开始 2：进行中 3：已结束
     */
    @TableField(value = "meeting_status")
    private Integer meetingStatus;

    /**
     * 会议创建类型 0:普通会议；1:快速会议
     */
    @TableField(value = "meeting_create_mode")
    private Integer meetingCreateMode;

    /**
     * 会议创建来源 0:空来源，1:客户端，2:web，3:企微，4:微信，5:outlook，6:restapi，7:腾讯文档
     */
    @TableField(value = "meeting_create_from")
    private Integer meetingCreateFrom;

    /**
     * 会议创建时间
     */
    @TableField(value = "operate_time")
    private Date operateTime;

    /**
     * 记录生成时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建人id（OAuth用户返回openId）
     */
    @TableField(value = "create_userid")
    private String createUserid;

    /**
     * 
     */
    @TableField(value = "create_open_id")
    private String createOpenId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 用户身份ID
     */
    @TableField(value = "create_uuid")
    private Integer createUuid;

    /**
     * 
     */
    @TableField(value = "create_ms_open_id")
    private Integer createMsOpenId;

    /**
     * 用户的终端设备类型 
     */
    @TableField(value = "create_instance_id")
    private Integer createInstanceId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}