package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/17
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class MeetingResourceAwardDTO implements Serializable {

    /**
     * 卓越卡号
     */
    private String joyoCode;

    /**
     * 资源类型
     */
    private Integer resourceType;

}
