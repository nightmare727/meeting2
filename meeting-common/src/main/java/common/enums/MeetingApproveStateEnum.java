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
public enum MeetingApproveStateEnum {
    /**
     * 会议状态 1：申请中 2：审批通过 3：审批驳回
     */

    APPLYING(1, "申请中"),
    APPROVED(2, "审批通过"),
    REJECTED(3, "审批驳回");
    private Integer state;

    private String desc;
}
