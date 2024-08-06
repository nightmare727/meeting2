package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/8/6
 * @Version 1.0
 * @Company: tiens
 */
@Data

//MeetingProfitPurchaseDetailStatusEnum
public class MeetingProfitPurchaseDetailVO implements Serializable {

    /**
     * 购买状态 1：可免费使用 2：免费资源已满，可购买收费资源优先预约  3:免费次数已用完，可购买收费资源。 4:使用高等级的资源，需要付费使用
     */
    private Integer purchaseStatus;

    /**
     * 最大持续时长
     */
    private Integer maxDuration;

}
