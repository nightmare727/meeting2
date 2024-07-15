package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户付费权益订单表
 * @TableName meeting_user_profit_order
 */
@TableName(value ="meeting_user_profit_order")
@Data
public class MeetingUserProfitOrderPO implements Serializable {
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
     * 订单编号
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 商品id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 订单状态
     */
    @TableField(value = "order_status")
    private Integer orderStatus;

    /**
     * 支付金额
     */
    @TableField(value = "paid_amount")
    private BigDecimal paidAmount;

    /**
     * 资源类型
     */
    @TableField(value = "resource_type")
    private Integer resourceType;

    /**
     * 资源描述
     */
    @TableField(value = "resource_desc")
    private String resourceDesc;

    /**
     * 持续时长
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