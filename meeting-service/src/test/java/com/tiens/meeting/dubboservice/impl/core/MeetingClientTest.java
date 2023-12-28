package com.tiens.meeting.dubboservice.impl.core;

import com.alibaba.fastjson.JSON;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;
import common.enums.MeetingRoomHandlerEnum;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/10
 * @Version 1.0
 * @Company: tiens
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MeetingClientTest {

    public String appId = "ce1860512edc4e77a288283d79f08a27";
    public String appKey = "502367f7fec27f77ddef76cd3b3129c71153431a9b45a5d4825e4f14e999d4dd";
    public String userId = "115e039f98e1441ba24e5e3584cef950";

    MeetingClient managerClient = null;
    MeetingClient userClient = null;

    @BeforeAll
    public void managerClient() {
        ICredential auth =
            new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(appId).withAppKey(appKey);
//            .withTenantScene()
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add("https://api.meeting.huaweicloud.com");
        endpoints.add("https://api-intl.meeting.huaweicloud.com");
        managerClient = MeetingClient.newBuilder().withCredential(auth).withEndpoints(endpoints).build();

        ICredential auth1 =
            new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(appId).withAppKey(appKey)
                .withUserId(userId);
//            .withTenantScene()

        userClient = MeetingClient.newBuilder().withCredential(auth1).withEndpoints(endpoints).build();

    }

    @Test
    public void searchCloudVmr() {
        //管理员查询会议列表
        SearchCorpVmrRequest searchCorpVmrRequest = new SearchCorpVmrRequest();
//        searchCorpVmrRequest.withSearchKey(userId);
        searchCorpVmrRequest.withVmrMode(1);
        SearchCorpVmrResponse searchCorpVmrResponse = managerClient.searchCorpVmr(searchCorpVmrRequest);
        List<QueryOrgVmrResultDTO> data1 = searchCorpVmrResponse.getData();
        System.out.println("云会议室资源列表结果：" + JSON.toJSONString(data1));
    }

    @Test
    public void searchSeminarVmr() {
        //管理员查询会议列表
        SearchCorpVmrRequest searchCorpVmrRequest = new SearchCorpVmrRequest();
//        searchCorpVmrRequest.withSearchKey(userId);
        searchCorpVmrRequest.withVmrMode(2);
        SearchCorpVmrResponse searchCorpVmrResponse = managerClient.searchCorpVmr(searchCorpVmrRequest);
        List<QueryOrgVmrResultDTO> data1 = searchCorpVmrResponse.getData();
        System.out.println("研讨会资源列表结果：" + JSON.toJSONString(data1));
    }

    /*@Test
    public void addUser() {
        AddUserRequest request = new AddUserRequest();
        AddUserDTO body = new AddUserDTO();
        body.withThirdAccount("3c707cb4cd7943c384ca6c9b70bcf7c8");
        body.withName("胡教粉");
        request.withBody(body);
        AddUserResponse response = managerClient.addUser(request);
        System.out.println(response.toString());
    }*/
    @Test
    @DisplayName("华为云SDK-研讨会详情接口")
    public void getWebinarDetail() {
        ShowWebinarRequest request = new ShowWebinarRequest();
        request.withConferenceId("985390630");
        ShowWebinarResponse response = userClient.showWebinar(request);
        System.out.println(response);
    }

    @Test
    @DisplayName("华为云SDK-云会议详情接口")
    public void showMeetingDetail() {
        ShowMeetingDetailRequest request = new ShowMeetingDetailRequest();
        request.withConferenceID("985390630");
        ShowMeetingDetailResponse response = managerClient.showMeetingDetail(request);
        System.out.println(response);
    }

    @Test
    @DisplayName("华为云SDK-资源获取接口")
    public void searchCorpVmr() {
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        request.withVmrMode(MeetingRoomHandlerEnum.CLOUD.getVmrMode());
        SearchCorpVmrResponse response2 = managerClient.searchCorpVmr(request);
        System.out.println(response2);

    }

    @Test
    @DisplayName("华为云SDK停止会议")
    public void stopMeeting() {
        CreateConfTokenRequest request = new CreateConfTokenRequest();
        request.withConferenceID("969644626");
        request.withXPassword("877574");
        request.withXLoginType(1);
        CreateConfTokenResponse response = managerClient.createConfToken(request);
        StopMeetingRequest stopMeetingRequest = new StopMeetingRequest();
        stopMeetingRequest.withConferenceID("969644626");
        stopMeetingRequest.withXConferenceAuthorization(response.getData().getToken());
        StopMeetingResponse stopMeeting = managerClient.stopMeeting(stopMeetingRequest);
        System.out.println(stopMeeting);
    }

}
