package com.tiens.meeting.dubboservice.config;

import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Configuration
public class HWMeetingConfiguration {

/*    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Bean
    public MeetingClient meetingClient(MeetingConfig meetingConfig) {
        MeetingCredentials auth =
            new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(meetingConfig.getAppId())
                .withAppKey(meetingConfig.getAppKey());
        MeetingClient client =
            MeetingClient.newBuilder().withCredential(auth).withEndpoints(meetingConfig.getEndpoints()).build();
        return client;
    }*/

}
