package common.exception.enums;

import common.exception.ErrorCode;

/**
 * 全局错误码枚举 0-999 系统异常编码保留
 *
 * 一般情况下，使用 HTTP 响应状态码 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status 虽然说，HTTP
 * 响应状态码作为业务使用表达能力偏弱，但是使用在系统层面还是非常不错的 比较特殊的是，因为之前一直使用 0 作为成功，就不使用 200 啦。
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode("0", "成功");

    // ========== 客户端错误段 ==========

    ErrorCode BAD_REQUEST = new ErrorCode("400", "请求参数不正确");
    ErrorCode UNAUTHORIZED = new ErrorCode("401", "账号未登录");
    ErrorCode FORBIDDEN = new ErrorCode("403", "没有该操作权限");
    ErrorCode NOT_FOUND = new ErrorCode("404", "请求未找到");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode("405", "请求方法不正确");
    ErrorCode LOCKED = new ErrorCode("423", "请求失败，请稍后重试"); // 并发请求，不允许
    ErrorCode TOO_MANY_REQUESTS = new ErrorCode("429", "请求过于频繁，请稍后重试");

    ErrorCode INVALID_ACC_ID = new ErrorCode("430", "无效的accId");

    // ========== 服务端错误段 ==========

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode("500", "系统异常");
    ErrorCode NOT_IMPLEMENTED = new ErrorCode("501", "功能未实现/未开启");

    // ========== 自定义错误段 ==========
    ErrorCode REPEATED_REQUESTS = new ErrorCode("900", "重复请求，请稍后重试"); // 重复请求
    ErrorCode DEMO_DENY = new ErrorCode("901", "演示模式，禁止写操作");

    ErrorCode UNKNOWN = new ErrorCode("999", "未知错误");

    ErrorCode OP_HW_CLIENT_ERROR = new ErrorCode("500", "操作华为云客户端异常");

    ErrorCode NOT_FOUND_HOST_INFO = new ErrorCode("2000001", "主持人信息不存在！");
    ErrorCode EXIST_HOST_INFO = new ErrorCode("2000002", "主持人信息已存在！");

    /**
     * 是否为服务端错误，参考 HTTP 5XX 错误码段
     *
     * @param code 错误码
     * @return 是否
     */
    static boolean isServerErrorCode(String code) {
        return true;
    }

}
