package com.tiens.meeting.web.entity.resp;

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
}
