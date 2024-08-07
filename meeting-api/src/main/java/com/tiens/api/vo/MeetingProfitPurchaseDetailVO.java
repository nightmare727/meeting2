package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/8/6
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class MeetingProfitPurchaseDetailVO implements Serializable {

    /**
     * 购买状态 1：可免费使用 2：免费资源已满，可购买收费资源优先预约  3:免费次数已用完，可购买收费资源。 4:使用高等级的资源，需要付费使用
     */
    private Integer purchaseStatus;

    /** 会议时长 */
    private List<Integer> durationList;

    // 30 60 90 120

    /** 提前入会时长，免费权益的，就返回上面那个数组        付费权益的，就找下面那个 */
    private List<Integer> leadTimeList;

}
