package com.tiens.meeting.util.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/8
 * @Version 1.0
 * @Company: tiens
 */
public class JWTTokenUtil {

    static String kid = "dc431302-8235-469e-9371-1c593064de8c";
    private static String SDKTokenEncode(String secret, Map<String, Object> headerClaims, Map<String, Object> payloadClaims) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                .withHeader(headerClaims)
                .withAudience((String) payloadClaims.get("aud"))
                .withExpiresAt((Date) payloadClaims.get("exp"))
                .withIssuedAt((Date) payloadClaims.get("iat"))
                .withIssuer((String) payloadClaims.get("iss"))
                .sign(algorithm);

            return token;
        } catch (JWTCreationException exception){
            //Invalid Signing configuration / Couldn't convert Claims.
        }
        return null;

    }

    public static String generateSDKToken() {
        //HMAC
        String sdkSecret = JWTConfig.HS256_Secret;
        Map<String, Object> sdkTokenHeaderClaims = new HashMap<String, Object>();
        Map<String, Object> sdkTokenPayloadClaims = new HashMap<String, Object>();
        //JWT Header
        sdkTokenHeaderClaims.put("alg", "HS256");
        sdkTokenHeaderClaims.put("typ", "JWT");
        //JWT Payload
        sdkTokenPayloadClaims.put("aud", "Tencent Meeting");
        sdkTokenPayloadClaims.put("iss", JWTConfig.SDK_ID);
        long iatSDKTokenTimeMillis = System.currentTimeMillis();
        long expSDKTokenTimeMillis = iatSDKTokenTimeMillis + JWTConfig.SDK_TOKEN_PERIOD_OF_VALIDITY;
        sdkTokenPayloadClaims.put("iat", new Date(iatSDKTokenTimeMillis));
        sdkTokenPayloadClaims.put("exp", new Date(expSDKTokenTimeMillis));

        String sdkToken = SDKTokenEncode(sdkSecret, sdkTokenHeaderClaims, sdkTokenPayloadClaims);

        return sdkToken;
    }

    private static String IDTokenEncode(RSAPublicKey publicKey, RSAPrivateKey privateKey, Map<String, Object> headerClaims, Map<String, Object> payloadClaims) {
        try {
            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);
            String token = JWT.create()
                .withHeader(headerClaims)
                .withSubject((String) payloadClaims.get("sub"))
                .withIssuer((String) payloadClaims.get("iss"))
                .withClaim("name", (String) payloadClaims.get("name"))
                .withExpiresAt((Date) payloadClaims.get("exp"))
                .withIssuedAt((Date) payloadClaims.get("iat"))
                .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            //Invalid Signing configuration / Couldn't convert Claims.
        }
        return null;

    }

    private static String generateIDToken(String userid, String username) throws IOException {
        //RSA
        RSAPublicKey publicKey = (RSAPublicKey) PemUtils.readPublicKeyFromFile(JWTConfig.PUBLIC_KEY_FILE_RSA, "RSA");//Get the key instance
        RSAPrivateKey privateKey = (RSAPrivateKey) PemUtils.readPrivateKeyFromFile(JWTConfig.PRIVATE_KEY_FILE_RSA, "RSA");//Get the key instance
        Map<String, Object> idTokenHeaderClaims = new HashMap<String, Object>();
        Map<String, Object> idTokenPayloadClaims = new HashMap<String, Object>();
        //JWT Header
        idTokenHeaderClaims.put("alg", "RS256");
        idTokenHeaderClaims.put("typ", "JWT");
        //JWT Payload
        idTokenPayloadClaims.put("iss", JWTConfig.SDK_ID);
        idTokenPayloadClaims.put("sub", userid);
        idTokenPayloadClaims.put("name", username);
        long iatIdTokenTimeMillis = System.currentTimeMillis();
        long expIdTokenTimeMillis = iatIdTokenTimeMillis + JWTConfig.ID_TOKEN_PERIOD_OF_VALIDITY;
        idTokenPayloadClaims.put("iat", new Date(iatIdTokenTimeMillis));
        idTokenPayloadClaims.put("exp", new Date(expIdTokenTimeMillis));

        String idToken = IDTokenEncode(publicKey, privateKey, idTokenHeaderClaims, idTokenPayloadClaims);

        return idToken;
    }

    public static String getSdkId() {
        return JWTConfig.SDK_ID;
    }

    public static String getSsoUrl(String userid, String username) {
        String idToken;
        try {
            idToken = generateIDToken(userid, username);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JWTConfig.SSO_URL+idToken;
    }




    /**
     * 生成JWT Token
     *
     * @param serviceAccount 秘钥字符串
     * @throws Exception
     * @author XiaohanLiu
     * @returnjwtToken jwt数据
     */
    public static String generateIDaasJWT(String serviceAccount) {
        try {
            //注意：这里获取到的时间戳需要是东8区的当前时间。
            long now = System.currentTimeMillis();
            JSONObject jwkJsonObj = JSON.parseObject(serviceAccount);
            String clientId = jwkJsonObj.getString("clientId");
            RSAPrivateKey privateKey = getPrivateKey(jwkJsonObj.getString("privateKey"));
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .audience("contacts")
                .expirationTime(new Date(now + 12*24*60*60*1000))  //Token的过期时间，此处为10分钟。
                .issueTime(new Date(now))
                .issuer(clientId)
                .claim("account_type", "serviceAccount");
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse("RS256"))
                .keyID(kid)
                .type(JOSEObjectType.JWT)
                .build();
            SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
            signedJWT.sign(new RSASSASigner(privateKey));
            return signedJWT.serialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将秘钥字符串转换成秘钥对象。
     *
     * @param privateKey 秘钥字符串
     * @return 秘钥对象
     * @throws Exception
     * @author XiaohanLiu
     */
    public static RSAPrivateKey getPrivateKey(String privateKey) throws Exception {
        Object privateKeyObj = new JSONParser(
            JSONParser.USE_HI_PRECISION_FLOAT |
                JSONParser.ACCEPT_TAILLING_SPACE).parse(privateKey);
        if (privateKeyObj instanceof com.nimbusds.jose.shaded.json.JSONObject) {
            com.nimbusds.jose.shaded.json.JSONObject jwtObject =
                (com.nimbusds.jose.shaded.json.JSONObject)privateKeyObj;
            // Find the RSA signing key
            if (jwtObject.get("use").equals("sig") && jwtObject.get("kty").equals("RSA")) {
                return RSAKey.parse(jwtObject).toRSAPrivateKey();
            }
        }
        return null;
    }
    public static void main(String[] args) throws IOException {

        String userid = "test";
        String username = "test";

        //SDK初始化参数获取
     /*   String sdkId = getSdkId();
        String sdkToken = generateSDKToken();
        System.out.println("SDK ID: " + sdkId);
        System.out.println("SDK Token: " + sdkToken);

        //SDK登录参数获取
        String ssoUrl = getSsoUrl(userid, username);
        System.out.println("username: " + userid);
        System.out.println("SSO URL: " + ssoUrl);*/

        String serviceAccount = "{\"clientId\":\"d87f9b23-509f-4543-9f06-abf6daf7db28\"," +
            "\"privateKey\":{\"d\":\"Z8jhSBfEpHXEP0WTeMxpPtbNVs2yzkkJxGoizZ7gVcY50QFXlkSE6gtcQyoXawkj7A1lY4ghn" +
            "-XxfZOliqusRwBNXUW4tCkzLpqMUiPk-TgOStq8yK_nH7SpLMFLmefLmrqgOGAieuRy-fUUhZcT6jBHx30B1qRe91O9p" +
            "--5EGMfq2Rb0eqsb_-8lunu04VC-gvguSvF0Y9mLQcCl" +
            "-lUIj9TyDvbNvCNMK2FMr8jkgsYljmNyDgfVn7iy1mSqUfEx3jPodKYXMfIBb6SNlUI1wrRk2Tqlre-1EMc6Y7_av3PUHmIn77x0" +
            "-9xHiOQbhmawZyhkiyx5XCbRSVq_3guGQ\",\"e\":\"AQAB\",\"kid\":\"dc431302-8235-469e-9371-1c593064de8c\"," +
            "\"use\":\"sig\"," +
            "\"dp" +
            "\":\"j61MhECAiBbGF3ZsmoAZQzwnhbM1IpGsvT4e51zSbe862uJGBOyzf8HEDCza2uR2a0L3DFvFRDDPVexhpK75strQCXQ5kU_9mvOKAZ9ShPahXWzyM8Pn2OqyxnxxhTCm4Bm5877yCXRqiuR56hOr1nHcJnKc8zY0UkLpK2la5wU\",\"dq\":\"mcMX0BDwcZS4BNXoenliyU46Iye1fZNA4RVppTvY6_pcqCy0FpWjfpmZIQG47x6pZJ3FPWiS_qcEm_j1hvNS1jUVjin7al6anq4V8TvESPm32YlU-yNrUklhr0nUEnazn6GQcl5I0YJ8a1btLX2ZPWiEMiWwpbB8Bmu3you2Srk\",\"n\":\"pF5cndvpuvZPbXeuR6Al4YapKKtypxgVg1GDd0pnWrCrSOB_CrJsagluE_8mXnH8As3uDYIWwPnwkW_wDarIJaRIUQlPdrkOa1hRGIShmJT3QHmZER8_FJfemiTDClJGufC2iuMe9Pzc2FoD6flQy9TIETP-Hf43RH6BgSALrj91SV9MbxVbHpvuw11BzNnPcaGRrTvmGpx9t-H1Vl3gATTFHgtAT5w2F9Yj73WBfK-hkJrFzsEpc6DiKXPiSKicIwtKQ03ZhZl5ShiRVVBer_3481hyOPEd3TVQ1XiH8J_i2wWPHqaXbHflejayDcqCNalL0tvMq7BdOkoY1cbDCw\",\"kty\":\"RSA\",\"p\":\"92PTnqSnjw320GXUgNhv1VBP3KrNZJ_bFMZ3yMPsU0AhNo3sAV0Ma0XMvCtBF1xvijnUwvYmmzF1dyrCmZiZXLMJAEHhSVzggJs2a4rIUz9AlQisgoXee9qTQCxIfStowPSsirhojLHb3S4oiKdFkrrBJGDt3apc9GLQCIS6-08\",\"q\":\"qhbWuyuVXpLeMNo_wwfTjrnVb5ETLpeFX1bNf9RMZNkoQaZmzSZRlLTbcn30t58_Q7S4MMzZopbijXU7-7efdXybZWBj62gG-I_1w9edOdTzD09aO4KU4_6Zke8pqf18TphJ2opWKMCaO1mX2WZbtDNXC19OgmCJrbXcnMeh3YU\",\"qi\":\"BikF2sOTBfWvNsgKrYmHA0c-tDc9UkuX4308QMnMcld_KdycqWaPsMdWpaPXhifXjI7BrEouAnmRZ5esEZ6Vfl18AKrZuza1ggVo0G_1j2a0bJ7WSQWjqsQAzQz_t-uK7Ta199vYR6OVbajGMKpbJh_zUNd2jHaCSimNqPncZUo\"}}";

        System.out.println(generateIDaasJWT(serviceAccount));

    }
}