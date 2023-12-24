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

    // ========== 自定义错误段 ==========
    ErrorCode REPEATED_REQUESTS = new ErrorCode("900", "重复请求，请稍后重试"); // 重复请求
    ErrorCode DEMO_DENY = new ErrorCode("901", "演示模式，禁止写操作");

    ErrorCode UNKNOWN = new ErrorCode("999", "未知错误");

    ErrorCode OP_HW_CLIENT_ERROR = new ErrorCode("500", "操作华为云客户端异常");

    ErrorCode NOT_IMPLEMENTED = new ErrorCode("501", "功能未实现/未开启");

    // ========== 服务端错误段 ==========
    ErrorCode INVALID_ACC_ID = new ErrorCode("430", "无效的accId");

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode("500", "系统异常");
    ErrorCode NOT_FOUND_HOST_INFO = new ErrorCode("2000001", "主持人信息不存在！");
    ErrorCode EXIST_HOST_INFO = new ErrorCode("2000002", "主持人信息已存在！");
    ErrorCode NOT_EXIST_ROOM_INFO = new ErrorCode("2000003", "会议号不存在或已结束！");

    ErrorCode EXIST_HOST_RESOURCE_CONFIGURATION = new ErrorCode("2000004", "主持人会议资源配置不合法");
    ErrorCode LEVEL_NOT_ENOUGH = new ErrorCode("2000005", "您的Vmo星球等级至少Lv3才可以使用此功能");
    ErrorCode NOT_EXIST_RESOURCE = new ErrorCode("2000006", "资源不存在，请刷新重试");
    ErrorCode RESOURCE_USED = new ErrorCode("2000007", "资源已被占用，请刷新重试");
    ErrorCode RESOURCE_MORE_THAN = new ErrorCode("2000008", "资源使用超出限制");
    ErrorCode CAN_NOT_MOD_MEETING_ROOM = new ErrorCode("2000009", "会议状态非预约状态，无法编辑，请刷新重试");
    ErrorCode CAN_NOT_CANCEL_MEETING_ROOM = new ErrorCode("2000010", "会议已结束，无法重复取消，请刷新重试");
    ErrorCode CAN_NOT_ALLOCATE_RESOURCE = new ErrorCode("2000011", "无法分配会议资源，会议状态异常，请刷新重试");
    ErrorCode CAN_NOT_CANCEL_ALLOCATE_RESOURCE =
        new ErrorCode("2000012", "无法取消分配会议资源，会议状态异常，请刷新重试");
    ErrorCode HW_CREATE_MEETING_ERROR = new ErrorCode("2000013", "创建会议错误，请稍后重试");
    ErrorCode HW_MOD_MEETING_ERROR = new ErrorCode("2000014", "编辑会议错误，请稍后重试");
    ErrorCode HW_CANCEL_MEETING_ERROR = new ErrorCode("2000015", "取消会议错误，请稍后重试");
    ErrorCode HW_START_TIME_ERROR =
        new ErrorCode("2000016", "会议开始时间错误，无法小于当前时间或者选择3个月后，请重新填写");
    ErrorCode NOT_ARRIVE_START_TIME_ERROR = new ErrorCode("2000017", "%s");
    ErrorCode CAN_NOT_USE_PERSONAL_RESOURCE_ERROR = new ErrorCode("2000018", "无权使用专属会议资源");
    ErrorCode RESOURCE_OPERATED_ERROR = new ErrorCode("2000019", "会议资源正被其他人操作中，请稍后再试");

    ErrorCode RESOURCE_REDISTRIBUTION_OPERATED_ERROR =
        new ErrorCode("2000020", "会议资源处于预分配状态中，无法操作，请稍后再试");

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
