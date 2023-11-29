package common.enums;

import common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 文件审批状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeleteEnum implements IntArrayValuable {

    NORMAL(0, "未删除"),
    DELETE(1, "删除");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(DeleteEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
