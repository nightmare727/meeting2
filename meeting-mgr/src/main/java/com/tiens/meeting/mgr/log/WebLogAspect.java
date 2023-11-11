package com.tiens.meeting.mgr.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

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
     * 以自定义 @WebLog 注解为切点
     */
    @Pointcut("@annotation(log.com.tiens.meeting.mgr.WebLog)")
    public void webLog() {
    }

    /**
     * 在切点之前织入
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes servletRequestAttributes =
            (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        String methodDescription = this.getAspectLogDescription(joinPoint);
        log.info("==========> Start <==========");
        log.info("URL: {}", httpServletRequest.getRequestURL().toString());
        log.info("Description: {}", methodDescription);
        log.info("HTTP Method Type: {}", httpServletRequest.getMethod());
        log.info("Class Method Type: {}.{}", joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName());
        log.info("Request Args: {}", JSON.toJSONString(joinPoint.getArgs()));
    }

    /**
     * 在切点之后织入
     *
     * @throws Exception
     */
    @After("webLog()")
    public void doAfter() throws Exception {
        log.info("==========> End <==========");
    }

    /**
     * 环绕织入
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info("Response Args: {}", JSON.toJSONString(result));
        log.info("Time Consuming: {} ms", System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 获取切面注解的描述
     *
     * @param joinPoint 切点
     * @return 描述信息
     * @throws Exception
     */
    public String getAspectLogDescription(JoinPoint joinPoint) throws Exception {
        String className = joinPoint.getTarget().getClass().getName();
        String methodNmae = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Class targetClass = Class.forName(className);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder();
        for (Method method : methods) {
            if (method.getName().equals(methodNmae)) {
                Class[] classes = method.getParameterTypes();
                if (classes.length == args.length) {
                    description.append(method.getAnnotation(WebLog.class).description());
                    break;
                }
            }
        }
        return description.toString();
    }
}
