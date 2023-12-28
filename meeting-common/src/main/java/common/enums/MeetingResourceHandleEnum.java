package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/8
 * @Version 1.0
 * @Company: tiens
 */

@AllArgsConstructor
@Getter
public enum MeetingResourceHandleEnum {

    /**
     * 占用
     */
    HOLD_UP,
    /**
     * 释放
     */
    HOLD_DOWN;
}
