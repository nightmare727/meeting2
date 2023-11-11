package com.tiens.meeting.web.util;

import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;

public class AddUserSolution {

    private static String appid = "ID89f4e01b24c54752aa6ef02c864efa42";
    private static String appkey = "3b6419481b81e65aee20946d84f1837924fe1b7c9d476fe781428e841217c72e";

    public static void main(String[] args) {
        String endpoint = "https://api.meeting.huaweicloud.com";

        ICredential auth = new MeetingCredentials()
            .withAuthType(AuthTypeEnum.APP_ID)
            .withAppId(appid)
            .withAppKey(appkey);

        MeetingClient client = MeetingClient.newBuilder()
            .withCredential(auth)
            .withEndpoint(endpoint)

            .build();
        AddUserRequest request = new AddUserRequest();
        request.withXRequestId("requestId");
        request.withAcceptLanguage("zh-CN");
        AddUserDTO body = new AddUserDTO();
        UserFunctionDTO functionbody = new UserFunctionDTO();
        functionbody.withEnableRoom(false);
        body.withSendNotify("0");
        body.withFunction(functionbody);
        body.withStatus(AddUserDTO.StatusEnum.NUMBER_0);
        body.withPhone("+8618911112222");
        body.withThirdAccount("user1");
        body.withEnglishName("zhangsan");
        body.withName("张三");
        request.withBody(body);
        try {
            AddUserResponse response = client.addUser(request);
            System.out.println(response.toString());
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
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