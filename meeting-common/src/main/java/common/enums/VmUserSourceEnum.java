package common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 终端的枚举
 */
@RequiredArgsConstructor
@Getter
/**
 * 1-买买 2-云购 3 Vshare 4 瑞狮 5意涵永
 */
public enum VmUserSourceEnum {
    /**
     * 健康说
     */
    JKS("1"),
    /**
     * 云购
     */
    YG("2"),
    /**
     * vshare
     */
    VSHARE("3"),
    /**
     * 瑞狮
     */
    RS("4"),
    /**
     * 意涵永
     */
    YHY("5");

    private final String code;

    public static String getNameByCode(String code) {
        for (VmUserSourceEnum value : VmUserSourceEnum.values()) {
            if (value.equals(code)) {
                return value.name();
            }
        }
        return null;
    }

}
