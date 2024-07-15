package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/5
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class UserMemberProfitModifyEntity implements Serializable {

    private String accId;

    private String vipLevel;

    private String joyoCode;

    private Integer memberLevel;


    //获取类型 1-充值 2-赠送 3-升级 4-降级
    private Integer getType;

    private Boolean giveUpgrade;

    private Integer operateMemberLevel;

    private Integer oldMemberLevel;
}
