package common.log;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/5
 * @Version 1.0
 * @Company: tiens
 */
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    public static final String TRACE_ID = "traceId";

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception arg3) throws Exception {


    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView arg3)
        throws Exception {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {

        String traceId = request.getHeader(TRACE_ID);

        if (StrUtil.isEmpty(traceId)) {
            MDC.put(TRACE_ID, UUID.fastUUID().toString(true));
        } else {
            MDC.put(TRACE_ID, traceId);
        }

        return true;
    }

}

