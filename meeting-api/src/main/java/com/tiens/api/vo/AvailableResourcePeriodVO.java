package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class AvailableResourcePeriodVO implements Serializable {

    private String beginTime;

    private String endTime;
}
