package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class CommonProfitConfigQueryVO implements Serializable {
    /**
     * 会员权益
     */
    private List<UserMemberProfitEntity> userMemberProfitList;
    /**
     * 首页展示配置
     */
    private String cmsShowFlag;
    /**
     * 付费预约限制
     */
    private String memberProfitFlag;

}
