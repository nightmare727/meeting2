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

    @Schema(description = "accid")
    private String accid;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "昵称")
    private String nickName;
    @Schema(description = "头像")
    private String headImg;
    @Schema(description = "状态")
    private String state;
    @Schema(description = "来源")
    private String source;
    @Schema(description = "关注数")
    private String followersNum;
    @Schema(description = "粉丝数")
    private String fansNum;
    @Schema(description = "性别")
    private String sex;
    private String personalProfile;
    private Integer levelCode;
    private Integer giftNumbers;
    @Schema(description = "国家")
    private String country;

    private String joyoCode;
}
