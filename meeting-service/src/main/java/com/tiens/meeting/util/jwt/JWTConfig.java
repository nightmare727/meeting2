package com.tiens.meeting.util.jwt;

import cn.hutool.core.io.FileUtil;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/8
 * @Version 1.0
 * @Company: tiens
 *
 * 全局参数配置 todo 迁移nacos
 */
public class JWTConfig {
    /**
     * 生成id Token 所需公钥
     */
    public static final String PUBLIC_KEY_FILE_RSA = "rsa_public_key.pem";
    /**
     * 生成id Token 所需密钥
     */
    public static final String PRIVATE_KEY_FILE_RSA = "rsa_private_key.pem";
    /**
     * SDK Secret：
     */
    public static final String HS256_Secret = "fSvNuwfCuUBJ85hi9Wlf2JcURrpQaZv1";
    public static final String SDK_ID = "24250186867";
    // 生成SDK token的有效期时长，不短于客户端登录态的有效期，单位毫秒
    public static final long SDK_TOKEN_PERIOD_OF_VALIDITY = 30*24*60*60*1000;
    // 生成ID token的有效期时长，5分钟即可，单位毫秒
    public static final long ID_TOKEN_PERIOD_OF_VALIDITY = 5*60*1000;
    public static final String SSO_URL = "https://test-idp.id.meeting.qq.com/cidp/custom/ai-2bd60857a7fd42a9b0887e321f16776a/ai-37586921eda647e580c409bef20a1b82?id_token=";

    public static void main(String[] args) {
        System.out.println(FileUtil.file(PUBLIC_KEY_FILE_RSA).getName());
    }
}