package com.tiens.meeting;

import cn.hutool.extra.spring.EnableSpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * @author admin
 */
@SpringBootApplication
@EnableDubbo
@ComponentScan(value = {"com.tiens", "com.jtmm"})
@MapperScan("com.tiens.meeting.repository.mapper")
@EnableSpringUtil
@EnableAspectJAutoProxy
@Slf4j
public class ServiceApplication {
    /**
     * 使用jar方式打包的启动方式
     */
    private static CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ServiceApplication.class, args).registerShutdownHook();
        COUNT_DOWN_LATCH.await();
    }

    @PostConstruct
    void setDefaultTimezone() {
        log.info("设置生产者默认时区为：{GMT}");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
//  TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }
}
