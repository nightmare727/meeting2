package com.tiens.meeting.dubboservice.common;

import java.lang.annotation.*;

/**
 * @author yuwenjie
 * @version: 0.1.0
 * @className: NewComerTasks
 * @description: 新人任务奖励注解
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface NewComerTasks {

    String description() default "";

}
