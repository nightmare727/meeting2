package com.tiens.meeting.web.interceptor;

import com.jtmm.third.party.wechat.company.WeChatCompanyService;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author gaofei
 */
@RestControllerAdvice
@Slf4j
public class ControllerException {

    @Resource
    WeChatCompanyService weChatCompanyService;

    @ExceptionHandler(Exception.class)
    public CommonResult handleException(HttpServletRequest request, Exception e) {
        log.error("服务器异常", e);
        weChatCompanyService.sendException(e, request.getRequestURI(), "");
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler(ServiceException.class)
    public CommonResult handleServiceException(HttpServletRequest request, ServiceException e) {
        log.error("业务异常", e);
        return CommonResult.error(e.getCode(), e.getMessage());
    }
}
