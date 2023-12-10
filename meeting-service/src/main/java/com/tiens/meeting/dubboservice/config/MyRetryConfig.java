package com.tiens.meeting.dubboservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/9
 * @Version 1.0
 * @Company: tiens
 */
@Configuration
public class MyRetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 配置重试策略
        RetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 配置重试间隔策略
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

}
