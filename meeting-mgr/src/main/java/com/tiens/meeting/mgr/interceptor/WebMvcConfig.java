package com.tiens.meeting.mgr.interceptor;

import common.log.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author gaofei
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InterfaceTimeConsuming()).addPathPatterns("/**");
        registry.addInterceptor(new LogInterceptor()).addPathPatterns("/**");
    }
}
