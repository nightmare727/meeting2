package com.tiens.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class AvailableResourcePeriodGetDTO implements Serializable {

    /**
     * (需要)资源id
     */
    private Integer resourceId;

    /**
     * (不需要)用户id
     */
    private String imUserId;
    /**
     * (需要)日期 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    /**
     * （需要传）时区偏移量
     */
    private String timeZoneOffset;

}
