package com.tiens.meeting.util.mdc;

import java.lang.annotation.*;

/**
 * @author: songxh@tiens.com
 * @version: 0.1.0
 * @className: MDCLog
 * @description: 自定义日志注解
 * @create: 2021/4/9
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface MDCLog {

    /**
     * 日志描述信息
     * @return
     */
    String description() default "";

}
