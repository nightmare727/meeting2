package com.tiens.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员权益配置表
 *
 * @TableName meeting_memeber_profit_config
 */
@TableName(value = "meeting_memeber_profit_config")
@Data
public class MeetingMemeberProfitConfigVO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 限制时间
     */
    @TableField(value = "limit_count")
    private Integer limitCount;

    /**
     * 规则类型 1：预约 2：快速
     */
    @TableField(value = "rule_type")
    private Integer ruleType;
    /**
     * 资源上限类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    @TableField(value = "resource_type")
    private String resourceType;

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
     * 提前入会的时间
     *
     */
    @TableField(value = "go_time")
    private String goTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}