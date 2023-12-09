package com.tiens.meeting.dubboservice.config;

import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
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

    public static void main(String[] args) {

       /* ICredential auth = new MeetingCredentials()
                .withAuthType(AuthTypeEnum.APP_ID)
                .withAppId("89f4e01b24c54752aa6ef02c864efa42")
                .withAppKey("3b6419481b81e65aee20946d84f1837924fe1b7c9d476fe781428e841217c72e");
                //.withUserId("4477b95a30b449be8aafe6bbfc69824d");


        AddUserRequest request = new AddUserRequest();
        AddUserDTO body = new AddUserDTO();

        body.withName(StrUtil.brief("nana", 64));
        body.setEmail("fhd787997");
        //userId
        body.withThirdAccount("747rt819t");
        //华为账号为卓越卡号拼接
        body.withAccount("194rehfdihf");
        request.withBody(body);

        try {
            MeetingClient meetingClient = MeetingClient.newBuilder()
                    .withCredential(auth)
                    .withEndpoint("https://api.meeting.huaweicloud.com")
                    .build();;
            AddUserResponse response = meetingClient.addUser(request);
            System.out.println(response);

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            if (e.getErrorCode().equals("USG.201040001")) {

            }
            throw new ServiceException("1000", e.getErrorMsg());
        }*/

        MeetingClient client = SpringUtil.getBean(MeetingClient.class);

        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        request.withVmrMode(2);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                MeetingResoucePO meetingResoucePO = new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getVmrId());
                meetingResoucePO.setVmrMode(2);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getVmrPkgParties());
                meetingResoucePO.setStatus(item.getStatus());
                Date date = new Date(item.getExpireDate());
                meetingResoucePO.setExpireDate(date);
                meetingResouceDaoService.insertMeetingResoucePO(meetingResoucePO);

            }
            System.out.println(response.toString());

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            System.out.println(e.getHttpStatusCode());
            System.out.println(e.getRequestId());
            System.out.println(e.getErrorCode());
            System.out.println(e.getErrorMsg());
        }

    }
}
