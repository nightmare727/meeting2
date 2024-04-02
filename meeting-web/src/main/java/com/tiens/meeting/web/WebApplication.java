package com.tiens.meeting.web;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(value = {"com.tiens", "com.jtmm"})
//@EnableMethodCache(basePackages = "com.tiens.meeting.web")
@EnableSpringUtil
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

   /* @PostConstruct
    void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
//  TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }*/
}
