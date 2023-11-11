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
public class VMUserVO implements Serializable {

    private String accid;
    private String mobile;
    private String email;
    private String nickName;
    private String headImg;
    private String state;
    private String source;
    private String followersNum;
    private String fansNum;
    private String sex;
    private String personalProfile;
    private Integer levelCode;
    private Integer giftNumbers;
    private String country;
}
