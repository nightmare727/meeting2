package common.exception;

import common.exception.enums.GlobalErrorCodeConstants;
import common.exception.enums.ServiceErrorCodeRange;
import lombok.Data;

import java.io.Serializable;

/**
 * 错误码对象
 *
 * 全局错误码，占用 [0, 999], 参见 {@link GlobalErrorCodeConstants} 业务异常错误码，占用 [1 000 000 000, +∞)，参见
 * {@link ServiceErrorCodeRange}
 *
 * TODO 错误码设计成对象的原因，为未来的 i18 国际化做准备
 */
@Data
public class ErrorCode implements Serializable {

    /**
     * 错误码
     */
    private final String code;
    /**
     * 错误提示
     */
    private final String chinesMsg;

    private final String wordKey;

    public ErrorCode(String code, String chinesMsg, String wordKey) {
        this.code = code;
        this.chinesMsg = chinesMsg;
        this.wordKey = wordKey;
    }

}
