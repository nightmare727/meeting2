package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/3
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class PushOrderDTO implements Serializable {
    /**
     * 国家编码
     */
    private String nationId;
    /**
     * accId
     */
    private String accId;

    /**
     * 卓越卡号
     */
    private String joyoCode;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * skuId
     */
    private String skuId;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 支付金额（VM币）
     */
    private BigDecimal paidVmAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal paidRealAmount;
    /**
     * 资源类型
     */
    private Integer resourceType;
    /**
     * 持续时长
     */
    private Integer duration;
}
