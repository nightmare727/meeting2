package com.tiens.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPaidProfitEntity implements Serializable {
    /**
     * 付费购买资源类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    private Integer resourceType;
    /**
     * 资源对应累计时长
     */
    private Integer accumulatedDuration;
}
