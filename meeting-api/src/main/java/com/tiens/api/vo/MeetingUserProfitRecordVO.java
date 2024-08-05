package com.tiens.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户权益使用记录表
 *
 * @TableName meeting_user_profit_record
 */
@TableName(value = "meeting_user_profit_record")
@Data
public class MeetingUserProfitRecordVO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 卓越卡号
     */
    @TableField(value = "joyo_code")
    private String joyoCode;

    /**
     * 初始会员类型 1：普通会员 2：V+ 3：红宝石V+ 4：蓝宝石V+
     */
    @TableField(value = "init_member_type")
    private Integer initMemberType;

    /**
     * 当前会员类型 1：普通会员 2：V+ 3：红宝石V+ 4：蓝宝石V+
     */
    @TableField(value = "current_member_type")
    private Integer currentMemberType;

    /**
     * 付费类型 1：会员权益 2：额外付费
     */
    @TableField(value = "paid_type")
    private Integer paidType;

    /**
     * 使用时间
     */
    @TableField(value = "use_time")
    private String useTime;

    /**
     * 会议id
     */
    @TableField(value = "meeting_id")
    private Long meetingId;

    /**
     * 会议真实时长（分钟数）
     */
    @TableField(value = "rel_duration")
    private Integer relDuration;
    /**
     * 会议锁定时长（分钟数）
     */
    @TableField(value = "lock_duration")
    private Integer lockDuration;

    /**
     * 资源类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    @TableField(value = "resource_type")
    private Integer resourceType;

    /**
     * 生效状态 0：失效 1：生效 2：预占用
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