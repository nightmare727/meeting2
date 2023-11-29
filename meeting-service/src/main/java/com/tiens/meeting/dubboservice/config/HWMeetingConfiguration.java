package com.tiens.meeting.dubboservice.config;

import java.util.List;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.AssociateVmrRequest;
import com.huaweicloud.sdk.meeting.v1.model.AssociateVmrResponse;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    /**
     * 普通用户维度的MeetingClient
     * @param meetingConfig
     * @param userId
     * @return
     */
    public static MeetingClient meetingClient(MeetingConfig meetingConfig,String userId) {
        MeetingCredentials auth =
                new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(meetingConfig.getAppId())
                        .withAppKey(meetingConfig.getAppKey()).withUserId(userId);
        MeetingClient client =
                MeetingClient.newBuilder().withCredential(auth).withEndpoints(meetingConfig.getEndpoints()).build();
        AssociateVmrRequest associateVmrRequest = new AssociateVmrRequest();

        associateVmrRequest.withBody(Lists.newArrayList("11121212"));
        //分配云会议
        AssociateVmrResponse associateVmrResponse = client.associateVmr(associateVmrRequest);


        return client;
    }

}
