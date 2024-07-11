package com.tiens.meeting.dubboservice.common.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class VMCoinsOperateModel implements Serializable {

    private String orderNo;
    private String country;
    private Integer source;
    private Integer amount;
    private Integer operateType;
    private Integer coinSource;

}
