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
public enum MeetingUserJoinStatusEnum implements Serializable {

    /**
     *
     */
    JOINED(1, "已加入"),
    /**
     *
     */
    UN_JOINED(2, "未加入");
    private Integer code;

    private String desc;

}
