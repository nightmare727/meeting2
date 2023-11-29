package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName meeting_time_zone_config
 */
@Data
public class MeetingTimeZoneConfigVO implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 时区id
     */
    private Integer timeZoneId;

    /**
     * 时区偏移量
     */
    private String timeZoneOffset;

    /**
     * 英文描述
     */
    private String englishDesc;

    /**
     * 中文描述
     */
    private String chineseDesc;

    private static final long serialVersionUID = 1L;

    
}