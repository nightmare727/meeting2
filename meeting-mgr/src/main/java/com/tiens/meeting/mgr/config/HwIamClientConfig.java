package com.tiens.meeting.mgr.config;

import com.huaweicloud.sdk.core.auth.GlobalCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.IdentityToken;
import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 蔚文杰
 * @Date: 2023/2/28
 * @Version 1.0
 */
@Configuration
public class HwIamClientConfig {

    @Value("${hw.ak}")
    private String ak;
    @Value("${hw.sk}")
    private String sk;

    @Value("${hw.region}")
    private String endPoint;

    @Bean
    IamClient iamClient() {
//        String ak = "B2JXZJNWTECGQ8VAIQQM";
//        String sk = "kEe4GBlY5m1l4f7lXc3DtBOffGMdy8IRD5xifenF";

        ICredential auth = new GlobalCredentials()
            .withAk(ak)
            .withSk(sk);

        IamClient client = IamClient.newBuilder()
            .withCredential(auth)
//            .withRegion(IamRegion.valueOf("cn-east-2"))
            .withRegion(IamRegion.valueOf(endPoint))
            .build();
        return client;
    }

    @Bean
    IdentityToken identityToken() {
        IdentityToken tokenIdentity = new IdentityToken();
        //1小时
        tokenIdentity.withDurationSeconds(60 * 60);
        return tokenIdentity;
    }

}
