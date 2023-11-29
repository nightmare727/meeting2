package com.tiens.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName meeting_host_user
 */
@Data
@Schema(description = "主持人用户信息实体")
public class MeetingHostUserVO implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主持人id")
    private Long id;

    /**
     * 云信userId
     */
    @Schema(description = "accid")
    private String accId;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    @Schema(description = "经销商编号")
    private String joyoCode;

}