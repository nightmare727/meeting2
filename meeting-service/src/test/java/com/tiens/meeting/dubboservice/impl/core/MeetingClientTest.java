package com.tiens.meeting.dubboservice.impl.core;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;
import common.enums.MeetingRoomHandlerEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Collections;
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
    //    public String appId = "89f4e01b24c54752aa6ef02c864efa42";
    public String appKey = "502367f7fec27f77ddef76cd3b3129c71153431a9b45a5d4825e4f14e999d4dd";
    //    public String appKey = "3b6419481b81e65aee20946d84f1837924fe1b7c9d476fe781428e841217c72e";
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
    public void getUseridDetail() {
        ShowUserDetailRequest request = new ShowUserDetailRequest();
        request.withAccount("90a2aed7dcaf45c398ccb39dc6a22f2b");
        request.withAccountType(1);
        ShowUserDetailResponse response = managerClient.showUserDetail(request);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void searchSeminarVmr() {
        //管理员查询会议列表
        SearchCorpVmrRequest searchCorpVmrRequest = new SearchCorpVmrRequest();
//        searchCorpVmrRequest.withSearchKey(userId);
        searchCorpVmrRequest.withVmrMode(1);
        SearchCorpVmrResponse searchCorpVmrResponse = managerClient.searchCorpVmr(searchCorpVmrRequest);
        List<QueryOrgVmrResultDTO> data1 = searchCorpVmrResponse.getData();
        System.out.println("研讨会资源列表结果：" + JSON.toJSONString(data1));
    }

    @Test
    public void addUser() {
        AddUserRequest request = new AddUserRequest();
        AddUserDTO body = new AddUserDTO();
        body.withThirdAccount("yuwenjie111222");
        body.withName("胡教粉");
        request.withBody(body);
        AddUserResponse response = managerClient.addUser(request);
        System.out.println(response.toString());
    }

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
    @DisplayName("华为云SDK-分配资源接口")
    public void fenPei() {
        AssociateVmrRequest request = new AssociateVmrRequest();
        request.withAccount("d45d2f5bb9144af9b1af766b54e3d195");
        request.withBody(Collections.singletonList("ee6f335f41524341b9d06ea01f778a50"));
        request.setAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        AssociateVmrResponse response = managerClient.associateVmr(request);
        System.out.println(response);

    }

    @Test
    @DisplayName("华为云SDK-取消资源接口")
    public void qx() {
        DisassociateVmrRequest request = new DisassociateVmrRequest();
        request.withAccount("6b91d8c60f2949feaf6725c5b380bd0a");
        request.withBody(Collections.singletonList("988994ebe1f8442eba39facd4d5f4d0c"));
        request.setAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        DisassociateVmrResponse response = managerClient.disassociateVmr(request);
        System.out.println(response);

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

    @Test
    @DisplayName("延长华为云会议")
    public void prolongMeeting() {

        String meetCode = "982357443";
        String hostPwd = "574948";
        CreateConfTokenRequest createConfTokenRequest = new CreateConfTokenRequest();
        createConfTokenRequest.withConferenceID(meetCode);
        createConfTokenRequest.withXPassword(hostPwd);
        createConfTokenRequest.withXLoginType(1);
        CreateConfTokenResponse response = managerClient.createConfToken(createConfTokenRequest);

        ProlongMeetingRequest request = new ProlongMeetingRequest();
        request.withConferenceID(meetCode);
        request.withXConferenceAuthorization(response.getData().getToken());
        RestProlongDurReqBody body = new RestProlongDurReqBody();
        body.withDuration(30);
        body.withAuto(1);
        request.withBody(body);
        ProlongMeetingResponse response1 = managerClient.prolongMeeting(request);
        System.out.println(response1);
    }

    @Test
    @DisplayName("华为云SDK-查询录制文件")
    public void queryRecordFiles() {
        ShowRecordingFileDownloadUrlsRequest request = new ShowRecordingFileDownloadUrlsRequest();
        request.withConfUUID("a6963021d25342b39645ea05425544bc");
        request.withLimit(500);
        ShowRecordingFileDownloadUrlsResponse response = managerClient.showRecordingFileDownloadUrls(request);
        List<RecordDownloadInfoBO> recordUrls = response.getRecordUrls();
        RecordDownloadInfoBO recordDownloadInfoBO = recordUrls.get(0);
        List<RecordDownloadUrlDO> urls = recordDownloadInfoBO.getUrls();
        System.out.println(urls);
    }

    @Test
    @DisplayName("测试集合差异")
    public void collDiff() {
        CollDiffInner collDiffInner = new CollDiffInner("name1", "age1");
        ArrayList<CollDiffInner> integers1 = Lists.newArrayList();
        ArrayList<CollDiffInner> integers2 = Lists.newArrayList();
        System.out.println("交集：" + CollectionUtil.intersection(integers1, integers2));
        System.out.println("差集1：" + CollectionUtil.subtractToList(integers1, integers2));
        System.out.println("差集2：" + CollectionUtil.subtractToList(integers2, integers1));

    }

    @Test
    @DisplayName("查询用户列表")
    public void searchUsers() {
        SearchUsersRequest request = new SearchUsersRequest();
        request.withAdminType(SearchUsersRequest.AdminTypeEnum.NUMBER_2);
        request.withOffset(1);
        request.setLimit(1);
        SearchUsersResponse searchUsersResponse = managerClient.searchUsers(request);
        System.out.println(searchUsersResponse);

    }

    //    会议主题:user_CB4B8CC1
//    会议时间:2024/01/29 12:00-13:00(GMT+08:00)
//    点击链接入会，或添加至会议列表:https://m-dev2.jikeint.com/conference/sharingConference?confId=965736955&nickName=user_CB4B8CC1&title=user_CB4B8CC1&lang=zh-CN&hostPwd=785777&guestPwd=&startTime=1706500800000&endTime=1706504400000
//
//    会议号 : 965736955
    @Test
    @DisplayName("createWebSocketToken")
    public void createWebSocketToken() {
        String code = "965736955";
        String pwd = "785777";

        TokenInfo data = getCreateConfToken(code, pwd).getData();
        System.out.println(JSON.toJSONString(data));

    }

    public CreateConfTokenResponse getCreateConfToken(String meetingCode, String hostPwd) {
        CreateConfTokenRequest request = new CreateConfTokenRequest();
        request.withConferenceID(meetingCode);
        request.withXPassword(hostPwd);
        request.withXLoginType(1);
        CreateConfTokenResponse response = managerClient.createConfToken(request);
        return response;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class CollDiffInner {

        String name;
        String age;

    }
}


