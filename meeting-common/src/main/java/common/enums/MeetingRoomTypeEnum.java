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
public enum MeetingRoomTypeEnum {
    /**
     * 资源状态 1:空闲 2:公有预约 3：付费预约 4：私人专属
     */

    /**
     * 空闲
     */
    FREE(1, "空闲"),
    /**
     * 公有预约
     */
    PUBLIC_SUBSCRIBE(2, "公有预约"),
    /**
     * 付费预约
     */
    PAID_SUBSCRIBE(3, "付费预约"),
    /**
     * 私人专属
     */
    PRIVATE(4, "私人专属");

    private Integer state;

    private String desc;

}
