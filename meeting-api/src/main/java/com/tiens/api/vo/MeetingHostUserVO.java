package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName meeting_host_user
 */
@Data
public class MeetingHostUserVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 云信userId
     */
    private String accId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 姓名
     */
    private String name;


}