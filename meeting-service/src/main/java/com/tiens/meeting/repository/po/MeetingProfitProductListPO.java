package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议权益商品列表
 * @TableName meeting_profit_product_list
 */
@TableName(value ="meeting_profit_product_list")
@Data
public class MeetingProfitProductListPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 权益编码
     */
    @TableField(value = "profit_code")
    private String profitCode;

    /**
     *  1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    @TableField(value = "resource_type")
    private Integer resourceType;

    /**
     * 持续时长（分钟数）
     */
    @TableField(value = "duration")
    private Integer duration;

    /**
     * vm币
     */
    @TableField(value = "vm_coins")
    private Integer vmCoins;

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