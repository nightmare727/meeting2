package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @Author: 谷守丙
 * @Date: 2023/12/5
 * @Version
 */
@AllArgsConstructor
@Getter
public enum MeetingUserJoinSourceEnum implements Serializable {

    /**
     * 预约
     */
    APPOINT(1, "预约"),
    /**
     * 中途加入
     */
    MIDWAY(2, "中途加入");
    private Integer code;

    private String desc;

}
