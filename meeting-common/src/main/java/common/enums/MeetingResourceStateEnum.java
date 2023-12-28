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
public enum MeetingResourceStateEnum {
    /**
     * 资源状态 1:公有空闲 2:公有预约 3:私有
     */
    PUBLIC_FREE(1, "公有空闲"),
    PUBLIC_SUBSCRIBE(2, "公有预约"),
    PRIVATE(3, "私有"),
    REDISTRIBUTION(4, "公有预分配");
    private Integer state;

    private String desc;

}
