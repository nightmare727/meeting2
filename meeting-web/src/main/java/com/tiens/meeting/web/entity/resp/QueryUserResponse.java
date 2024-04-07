package com.tiens.meeting.web.entity.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 〈〉
 *
 * @author yangshibo
 * @create 2021/6/22
 * @since 1.0.0
 */
@Data
public class QueryUserResponse {

    private String userId;

    private String nickName;

    private String userPhone;

    private String userPhoto;

    private String inviteCode;

    @Schema(description = "国家")
    private String country;

    /**
     * 粉丝数
     */
    @Schema(description = "粉丝数")
    private String fansNum;


    /**
     * 级别
     */
    private Integer levelCode;

}
