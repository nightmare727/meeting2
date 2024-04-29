package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会议多人奖励记录表
 *
 * @TableName meeting_multi_person_award_record
 */
@TableName(value = "meeting_multi_person_award_record")
@Data
public class MeetingMultiPersonAwardRecordPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 会议id
     */
    @TableField(value = "meeting_id")
    private Long meetingId;

    /**
     * 会议号
     */
    @TableField(value = "meeting_code")
    private String meetingCode;

    /**
     * 会议真实人数
     */
    @TableField(value = "meeting_rel_person_count")
    private Integer meetingRelPersonCount;

    /**
     * 奖励阈值数
     */
    @TableField(value = "award_count")
    private Integer awardCount;
    /**
     * 同步结果  -1：初始化  0：失败 1：成功
     */
    @TableField(value = "sync_result")
    private Integer syncResult;
    /**
     */
    @TableField(value = "im_user_id")
    private String imUserId;

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