package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class CommonProfitConfigSaveDTO implements Serializable {

    /**
     * 首页展示配置
     */
    private String cmsShowFlag;
    /**
     * 付费预约限制
     */
    private String memberProfitFlag;
}
