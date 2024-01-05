package com.tiens.meeting.web.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static common.log.LogInterceptor.TRACE_ID;

/**
 * @author gaofei
 */
@Slf4j
public class InterfaceTimeConsuming implements HandlerInterceptor {

    private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<Long>("StartTime-EndTime");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        Long startTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        startTimeThreadLocal.set(startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        StringBuilder sb = new StringBuilder();
        Long startTime = startTimeThreadLocal.get();
        Long endTime = System.currentTimeMillis();
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod)handler;
            String className = method.getBean().getClass().getSimpleName();
            String methodName = method.getMethod().getName();
            Long time = endTime - startTime;
            sb.append("[" + time / 1000.0 + "s]" + className + "." + methodName + ",url=>" + request.getRequestURI());
            log.info(sb.toString());
        }
        startTimeThreadLocal.remove();
        MDC.remove(TRACE_ID);
    }

}
