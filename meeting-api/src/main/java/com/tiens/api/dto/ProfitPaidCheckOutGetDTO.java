package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/8/6
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class ProfitPaidCheckOutGetDTO implements Serializable {

    /** 资源类型 */
    private String resourceType;

    /**  持续时长 */
    private Integer duration;

    /** 前置时间 */
    private Integer leadTime;
}
