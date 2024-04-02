package com.tiens.meeting.web.entity.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/4/2
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class BatchQueryUserResponse implements Serializable {

    /**
     * 查询唯一标识
     */
    private String uniqueSign;

    /**
     * accid
     */
    private String accid;

    /**
     * 用户数据
     */
    private QueryUserResponse data;
}
