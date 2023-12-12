package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class CancelResourceAllocateDTO implements Serializable {
    /**
     * 资源id
     */
    private Integer resourceId;
}
