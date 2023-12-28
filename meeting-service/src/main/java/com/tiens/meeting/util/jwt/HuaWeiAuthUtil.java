package com.tiens.meeting.util.jwt;

import cn.hutool.crypto.SecureUtil;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
public class HuaWeiAuthUtil {

    @SneakyThrows
    public static String calculateHmacSha256(String message, String key) {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacSha256Bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacSha256Bytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(calculateHmacSha256("hello","world"));

    }
}