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
public enum CommonStateEnum {
    /**
     * 0：无效 1：生效
     */

    INVALID(0, "无效"),
    VALID(1, "有效");
    private Integer state;

    private String desc;
}
