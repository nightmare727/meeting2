package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName meeting_client_version
 */
@Data
public class MeetingClientVersionDTO implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 类型 windows、mac
     */
    private String type;

    /**
     * 版本
     */
    private String version;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

}