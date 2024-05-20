package com.tiens.meeting.util.mdc;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

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
    }

    /**
     * 在切点之后织入
     *
     * @throws Exception
     */
    @After("MDCLog()")
    public void doAfter() throws Exception {
        //先于doAround执行
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
        String traceId = UUID.fastUUID().toString(true);
        MDC.put(TRACE_ID, traceId);
        String description = getDescription(proceedingJoinPoint);

        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info("【{}】【定时任务执行】耗时: {} ms", description, System.currentTimeMillis() - startTime);
        MDC.remove(TRACE_ID);
        return result;
    }

    String getDescription(ProceedingJoinPoint jp) throws NoSuchMethodException {
        //1.获取目标对象类型
        Class<?> targetCls = jp.getTarget().getClass();
//2.获取目标方法对象
//2.1获取方法签名信息
        MethodSignature ms = (MethodSignature)jp.getSignature();
//2.2获取方法对象
//假如底层配置为jdk代理,则method指向接口中的抽象方法对象.
//假如底层配置为CGLIB代理,则这个method指向具体目标对象中的方法对象
//Method method=ms.getMethod();
//假如希望无论是jdk代理还是cglib代理,我们让method变量指向的都是目标对象中的方法对象,那如何实现?
        Method method = targetCls.getDeclaredMethod(ms.getName(), ms.getParameterTypes());
//3.获取方法上的reqiredLog注解对象
        MDCLog requiredLog = method.getAnnotation(MDCLog.class);
//4.获取注解中的operation的值.
        return requiredLog.description();
    }
}
