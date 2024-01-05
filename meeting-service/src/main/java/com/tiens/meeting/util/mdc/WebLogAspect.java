package com.tiens.meeting.util.mdc;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static common.log.LogInterceptor.TRACE_ID;

/**
 * @author: songxh@tiens.com
 * @version: 0.1.0
 * @className: WebLogAspect
 * @description: 日志切面织入
 * @create: 2021/4/9
 **/
@Slf4j
@Aspect
@Component
public class WebLogAspect {

    /**
     * 以自定义 @MDCLog 注解为切点
     */
    @Pointcut("@annotation(com.tiens.meeting.util.mdc.MDCLog)")
    public void MDCLog() {
    }

    /**
     * 在切点之前织入
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("MDCLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        String traceId = MDC.get(TRACE_ID);
        if (StrUtil.isBlank(traceId)) {
            traceId = UUID.fastUUID().toString(true);
            MDC.put(TRACE_ID, traceId);
        }
    }

    /**
     * 在切点之后织入
     *
     * @throws Exception
     */
    @After("MDCLog()")
    public void doAfter() throws Exception {
        MDC.remove(TRACE_ID);
    }

    /**
     * 环绕织入
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("MDCLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info("【定时任务执行】耗时: {} ms", System.currentTimeMillis() - startTime);
        return result;
    }

}
