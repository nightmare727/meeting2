package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/7
 * @Version 1.0
 * @Company: tiens
 */
@Getter
@AllArgsConstructor
public enum MeetingResoyrceDateEnum {
    /**
     * 时间
     */
    DATE_30(1, 30),
    /**
     * 研讨会
     */
    DATE_60(2, 60),
    ;

    private Integer vmrMode;

    private Integer handlerName;

    public static Integer getHandlerNameByVmrMode(Integer vmrMode) {
        for (MeetingResoyrceDateEnum value : MeetingResoyrceDateEnum.values()) {
            if (value.getVmrMode().equals(vmrMode)) {
                return value.getHandlerName();
            }
        }
        return null;
    }
}
