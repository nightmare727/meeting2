package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class CmsShowVO implements Serializable {
    /**
     * 会员权益
     */
    private List<UserMemberProfitEntity> userMemberProfitEntityList;

    /**
     * 设备配置建议
     */
    private String deviceSuggestion;

    /**
     * 拓展信息
     */
    private String extra;
}
