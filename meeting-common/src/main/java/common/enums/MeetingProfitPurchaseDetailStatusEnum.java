package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 */
@AllArgsConstructor
@Getter
public enum MeetingProfitPurchaseDetailStatusEnum {
    /**
     * * 购买状态 1：可免费使用 2：免费资源已满，可购买收费资源优先预约  3:免费次数已用完，可购买收费资源。 4:使用高等级的资源，需要付费使用 5：无法使用
     */
    FREE_USE(1, "可免费使用"),
    FREE_RESOURCE_MAX(2, "免费资源已满，可购买收费资源优先预约"),
    FREE_TIME_USERD(3, "免费次数已用完，可购买收费资源"),
    PAY_FOR_HIGHT_RESOURCE(4, "使用高等级的资源，需要付费使用"),
    CAN_NOT_USE(5, "无法使用");
    private Integer state;

    private String desc;

}
