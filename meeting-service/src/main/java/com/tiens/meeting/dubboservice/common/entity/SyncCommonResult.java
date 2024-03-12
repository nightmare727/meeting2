package com.tiens.meeting.dubboservice.common.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/3/12
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class SyncCommonResult implements Serializable {

    private Boolean success;

    private String code;

    private String message;
}
