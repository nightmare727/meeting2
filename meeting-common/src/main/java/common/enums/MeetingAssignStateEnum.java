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
public enum MeetingAssignStateEnum {
    /**
     * 会议资源分配状态
     */
    UN_ASSIGN_STATE_ENUM(0, "未分配"),
    ASSIGN_STATE_ENUM(1, "已分配");
    private Integer state;

    private String desc;

}
