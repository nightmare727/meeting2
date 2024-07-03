package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员权益配置表
 * @TableName meeting_memeber_profit_config
 */
@TableName(value ="meeting_memeber_profit_config")
@Data
public class MeetingMemeberProfitConfigPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 会员类型
     */
    @TableField(value = "member_type")
    private Integer memberType;

    /**
     * 每日免费预约会议次数
     */
    @TableField(value = "free_day_appoint_count")
    private Integer freeDayAppointCount;

    /**
     * 限制次数
     */
    @TableField(value = "limit_count")
    private Integer limitCount;

    /**
     * 规则类型 1：预约 2：快速
     */
    @TableField(value = "rule_type")
    private Integer ruleType;

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