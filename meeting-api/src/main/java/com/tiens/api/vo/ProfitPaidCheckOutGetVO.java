package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: 蔚文杰
 * @Date: 2024/8/6
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class ProfitPaidCheckOutGetVO implements Serializable {

    /**
     * 需要支付VM币额度
     */
    private BigDecimal needPayVMAmount;
    /**
     * 需要支付实际额度
     */
    private BigDecimal needPayAmount;
}
