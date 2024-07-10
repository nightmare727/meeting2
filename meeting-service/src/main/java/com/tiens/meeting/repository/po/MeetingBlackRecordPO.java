package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 黑名单记录表
 *
 * @TableName meeting_black_record
 */
@TableName(value = "meeting_black_record")
@Data
public class MeetingBlackRecordPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 会议号
     */
    @TableField(value = "meeting_code")
    private String meetingCode;

    /**
     * 会议id
     */
    @TableField(value = "meeting_id")
    private Long meetingId;

    /**
     * 用户accId
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 0：无效 1：有效
     */
    @TableField(value = "status")
    private Integer status;

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
}