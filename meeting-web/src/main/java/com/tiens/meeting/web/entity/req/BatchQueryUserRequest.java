package com.tiens.meeting.web.entity.req;

import lombok.Data;

import java.util.List;

/**
 * 〈〉
 *
 * @author yangshibo
 * @create 2021/6/22
 * @since 1.0.0
 */
@Data
public class BatchQueryUserRequest {
    private List<QueryUserRequest> queryUserRequestList;

}
