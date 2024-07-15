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
public enum PaidTypeEnum {
    /**
     * 0：免费 1：收费
     */

    MEMBER_FREE(0, "免费"),
    PAID(1, "收费");

    private Integer state;

    private String desc;
}
