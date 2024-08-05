package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class ResourceAllocateDTO implements Serializable {

    private String joyoCode;
    private Integer resourceId;

    /**
     * 私有资源到期日期
     */
    private Date ownerExpireDate;
}
