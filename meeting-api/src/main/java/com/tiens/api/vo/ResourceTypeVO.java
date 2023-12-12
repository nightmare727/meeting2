package com.tiens.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/12
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceTypeVO implements Serializable {
    /**
     * 类型
     */
    private Integer type;
    /**
     * 描述
     */
    private String desc;
}
