package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName meeting_client_version
 */
@Data
public class MeetingClientVersionVO implements Serializable {
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

    private String downloadUrl;

    private static final long serialVersionUID = 1L;

}