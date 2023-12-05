package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class AvailableResourcePeriodGetDTO implements Serializable {

    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 用户id
     */
    private String imUserId;
    /**
     * 日期 yyyy-MM-dd
     */
    private String date;

}
