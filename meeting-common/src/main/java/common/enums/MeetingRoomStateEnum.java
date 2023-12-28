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
public enum MeetingRoomStateEnum {
    /**
     * 会议状态。 ● “Schedule”：预定状 态 ● “Creating”：正在创 建状态 ● “Created”：会议已 经被创建，并正在召开 ● “Destroyed”：会议 已经关闭
     */

    Schedule("Schedule", "预定状态"), Creating("Creating", "正在创建状态"),
    Created("Created", "会议已经被创建，并正在召开"), Destroyed("Destroyed", "会议已经关闭"),
    ;
    private String state;

    private String desc;
}
