package com.tiens.meeting.web.entity.req;

import lombok.Data;

/**
 * 〈〉
 *
 * @author yangshibo
 * @create 2021/6/22
 * @since 1.0.0
 */
@Data
public class QueryUserRequest {
    /**
     * 查询唯一标识
     */
    private String uniqueSign;

    /**
     * accid
     */
    private String accid;
}
