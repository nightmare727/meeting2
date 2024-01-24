package com.tiens.meeting.web.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.*;
import com.tiens.meeting.web.entity.resp.CredentialResponse;
import common.exception.ServerException;
import common.exception.enums.GlobalErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/2/28
 * @Version 1.0
 */
@Slf4j
public class HwClientUtil {

    public static CredentialResponse getTemporaryAccessKeyByToken() {
        IamClient client = SpringUtil.getBean(IamClient.class);
        IdentityToken tokenIdentity = SpringUtil.getBean(IdentityToken.class);
        CreateTemporaryAccessKeyByTokenRequest request = new CreateTemporaryAccessKeyByTokenRequest();
        CreateTemporaryAccessKeyByTokenRequestBody body = new CreateTemporaryAccessKeyByTokenRequestBody();

        List<TokenAuthIdentity.MethodsEnum> listIdentityMethods = new ArrayList<>();
        listIdentityMethods.add(TokenAuthIdentity.MethodsEnum.fromValue("token"));
        TokenAuthIdentity identityAuth = new TokenAuthIdentity();
        identityAuth.withMethods(listIdentityMethods);
        TokenAuth authbody = new TokenAuth();
        authbody.withIdentity(identityAuth);
        identityAuth.withToken(tokenIdentity);
        body.withAuth(authbody);
        request.withBody(body);
        try {
            CreateTemporaryAccessKeyByTokenResponse response = client.createTemporaryAccessKeyByToken(request);
            return (BeanUtil.copyProperties(response.getCredential(),CredentialResponse.class));
        } catch (ConnectionException e) {
            e.printStackTrace();
            log.error("连接华为云异常！", e);
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
            log.error("请求华为云异常！", e);
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            log.error("华为云响应异常！", e);
        }
        throw new ServerException(GlobalErrorCodeConstants.OP_HW_CLIENT_ERROR);
    }
}
