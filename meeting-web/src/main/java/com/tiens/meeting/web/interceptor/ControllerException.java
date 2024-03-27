package com.tiens.meeting.web.interceptor;

import cn.hutool.core.util.ObjectUtil;
import com.jtmm.third.party.wechat.company.WeChatCompanyService;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

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
        log.info("所有请求头信息：{}", getAllHeaders(request));
        String nationId = request.getHeader("nation_id");
        String platform = request.getHeader("p");
        String version = request.getHeader("v");

        String systemVersion = ObjectUtil.defaultIfBlank(nationId + "-" + platform + "-" + version, "未知版本");
        weChatCompanyService.sendException(e, request.getRequestURI(), systemVersion);
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler(ServiceException.class)
    public CommonResult handleServiceException(HttpServletRequest request, ServiceException e) {
        log.error("业务异常", e);
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    String getAllHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append("\n");
        }
        return headers.toString();

    }

}
