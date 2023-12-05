package com.tiens.meeting.dubboservice.config;

import java.util.Date;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import com.tiens.meeting.repository.service.MeetingResouceDaoService;
import common.exception.ServiceException;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.assertj.core.util.Lists;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Configuration
public class HWMeetingConfiguration {

    @Autowired
    private static MeetingResouceDaoService meetingResouceDaoService;

    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    public MeetingClient meetingClient(MeetingConfig meetingConfig) {
        MeetingCredentials auth =
                new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(meetingConfig.getAppId())
                        .withAppKey(meetingConfig.getAppKey());
        MeetingClient client =
                MeetingClient.newBuilder().withCredential(auth).withEndpoints(meetingConfig.getEndpoints()).build();
        return client;
    }

}
