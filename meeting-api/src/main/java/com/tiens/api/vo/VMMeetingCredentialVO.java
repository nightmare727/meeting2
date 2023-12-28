package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class VMMeetingCredentialVO implements Serializable {
    /**
     * 签证
     */
    private String signature;
    /**
     * 过期时间戳
     */
    private Integer expireTime;
    /**
     * 随机字符串
     */
    private String nonce;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名称
     */
    private String userName;
}
