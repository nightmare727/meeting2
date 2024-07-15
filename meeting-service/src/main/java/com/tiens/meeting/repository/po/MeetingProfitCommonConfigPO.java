package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会议权益公共配置表
 *
 * @TableName meeting_profit_common_config
 */
@TableName(value = "meeting_profit_common_config")
@Data
public class MeetingProfitCommonConfigPO implements Serializable {
    /**
     * 主键
     */
    @TableField(value = "id")
    private Integer id;

    /**
     * 配置key
     */
    @TableId(value = "config_key")
    private String configKey;

    /**
     *
     */
    @TableField(value = "config_value")
    private String configValue;

    /**
     *
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}