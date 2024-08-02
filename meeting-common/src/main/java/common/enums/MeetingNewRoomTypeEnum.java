package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: MiaoQ
 * @Date: 2024/08/02
 * @Version 1.0
 * @Company: tiens
 * 会议室类型
 */
@AllArgsConstructor
@Getter
public enum MeetingNewRoomTypeEnum {
    /**
     * 会议室类型 0未分配 2公有 3私有 4付费
     */

    INIT(0, "未分配"),
    /**
     * 公有预约
     */
    PUBLIC(2, "公有预约"),
    /**
     * 付费预约
     */
    PAID(3, "付费预约"),
    /**
     * 私人专属
     */
    PRIVATE(4, "私人专属");

    private Integer state;

    private String desc;

}
