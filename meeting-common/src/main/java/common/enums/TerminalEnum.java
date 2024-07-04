package common.enums;

import common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 终端的枚举
 */
@RequiredArgsConstructor
@Getter
public enum TerminalEnum implements IntArrayValuable {

    WECHAT_MINI_PROGRAM(10, "微信小程序"),
    WECHAT_WAP(11, "微信公众号"),
    H5(20, "H5 网页"),
    IOS(2, "苹果 App"),
    ANDROID(1, "安卓 App"),
    WINDOWS(3, "windows"),
    MAC(4, "MAC"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TerminalEnum::getTerminal).toArray();


    public static TerminalEnum getByTerminal(Integer terminal) {
        for (TerminalEnum value : TerminalEnum.values()) {
            if (value.getTerminal().equals(terminal)) {

                return value;
            }

        }

        return null;
    }

    /**
     * 终端
     */
    private final Integer terminal;
    /**
     * 终端名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
