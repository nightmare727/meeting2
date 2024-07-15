package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户付费权益表
 * @TableName meeting_user_paid_profit
 */
@TableName(value ="meeting_user_paid_profit")
@Data
public class MeetingUserPaidProfitPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户accid
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 卓越卡号
     */
    @TableField(value = "joyo_code")
    private String joyoCode;

    /**
     * 资源类型
     */
    @TableField(value = "resource_type")
    private Integer resourceType;

    /**
     * 持续时长（分钟）
     */
    @TableField(value = "duration")
    private Integer duration;

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