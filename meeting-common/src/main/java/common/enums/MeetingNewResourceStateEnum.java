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
public enum MeetingNewResourceStateEnum {
    /**
     * 资源状态 1:空闲 2:有预约
     */
    FREE(1, "空闲"), SUBSCRIBE(2, "有预约");

    private Integer state;

    private String desc;

}
