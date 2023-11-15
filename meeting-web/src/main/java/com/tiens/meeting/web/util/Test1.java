package com.tiens.meeting.web.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.huaweicloud.sdk.meeting.v1.utils.HmacSHA256;

import java.io.IOException;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/10
 * @Version 1.0
 * @Company: tiens
 */

public class Test1 {
    public static String appId = "89f4e01b24c54752aa6ef02c864efa42";
    public static String appKey = "3b6419481b81e65aee20946d84f1837924fe1b7c9d476fe781428e841217c72e";

    public static String nonce = "0388768046454097434647552987669536758841";

    public static void main(String[] args) throws IOException {

        String userId = "我的天呢";
        Long expireTime = 0L;
        String data = appId + ":" + userId + ":" + expireTime + ":" + nonce;
        System.out.println("SHA256加密明文：" + data);
        String authorization = "HMAC-SHA256 signature=" + HmacSHA256.encode(data, appKey);
        System.out.println("管理员永久Signature：" + authorization);


        String requestStr =
            JSONUtil.createObj().set("appId", appId).set("userId", userId).set("expireTime", expireTime).set("nonce",
                    nonce)
                .set("clientType", 72).toString();
        System.out.println("接口入参：" + requestStr);
        HttpResponse execute = HttpUtil.createPost("https://api.meeting.huaweicloud.com/v2/usg/acs/auth/appauth")
            .header("Authorization", authorization).body(requestStr, "application/json; charset=UTF-8").execute();
        System.out.println("接口返回："+execute.body());
    }


}
