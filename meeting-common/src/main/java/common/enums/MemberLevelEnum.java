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
public enum MemberLevelEnum {
    /**
     * 1:普通用户 10：V+ 20：红宝 30：蓝宝石
     */

    NORMAL(1, "普通用户"), VPLUS(10, "V+"), RED(20, "红宝"), BLUE(30, "蓝宝石");
    private Integer state;

    private String desc;
}
