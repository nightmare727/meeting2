package com.tiens.meeting.mgr.interceptor;


import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author gaofei
 */
@RestControllerAdvice
@Slf4j
public class ControllerException {



    @ExceptionHandler(ServiceException.class)
    public CommonResult handleException(ServiceException e) {
        log.error("业务异常", e);
        return CommonResult.error(e.getErrorCode());
    }
    @ExceptionHandler(Exception.class)
    public CommonResult handleException(Exception e) {
        log.error("服务器异常", e);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }



}
