package com.tiens.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
@Schema(description = "Vmoment用户信息实体")
public class VMUserVO implements Serializable {
    /**
     * accid
     */
    @Schema(description = "accid")
    private String accid;
    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String mobile;
    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;
    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickName;
    /**
     * 头像地址
     */
    @Schema(description = "头像")
    private String headImg;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String state;
    /**
     * 来源
     */
    @Schema(description = "来源")
    private String source;
    /**
     * 关注数
     */
    @Schema(description = "关注数")
    private String followersNum;
    /**
     * 粉丝数
     */
    @Schema(description = "粉丝数")
    private String fansNum;
    /**
     * 性别
     */
    @Schema(description = "性别")
    private String sex;
    private String personalProfile;
    /**
     * 级别
     */
    private Integer levelCode;
    private Integer giftNumbers;
    @Schema(description = "国家")
    private String country;

    private String joyoCode;

    /**
     * 会员等级
     */
    private Integer memberType;

}
