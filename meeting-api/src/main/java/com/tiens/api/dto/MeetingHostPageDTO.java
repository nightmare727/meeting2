package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class MeetingHostPageDTO implements Serializable {

    private String name;

    private String phone;

    private String userId;
}
