/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tiens.meeting.web.controller;

import com.tiens.meeting.web.entity.common.CommonWeHookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@RestController
@Slf4j
public class MeetingController {

    String token = "FOVAN9JdTLsizOpyQKCQwPrKo";

    String encodingAESKey = "76Sj3yUfCBHnGlM5Vr36kCZR9pBXhwsFmVBr2sXBB5z";

    public static void main(String[] args) {
//        "start_time":1698465860,"end_time":1698469460,
        System.out.println((1698469460 - 1698465860));
    }

    /**
     * 解密
     *
     * @param encryptedText
     * @param key
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptedText, String key) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] keyBytes = Base64.getDecoder().decode(key + "=");
        byte[] ivBytes = new byte[16];
        System.arraycopy(keyBytes, 0, ivBytes, 0, 16);

        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        int paddingLength = decryptedBytes[decryptedBytes.length - 1];
        byte[] unpaddedData = new byte[decryptedBytes.length - paddingLength];
        System.arraycopy(decryptedBytes, 0, unpaddedData, 0, unpaddedData.length);
        return new String(unpaddedData, StandardCharsets.UTF_8);
    }

    /**
     * 此接口用于腾讯会议验证事件回调地址有效性
     * @param name
     * @param checkStr
     * @param map
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/")
    public String check(@RequestParam(name = "name", defaultValue = "unknown user") String name,
        @RequestParam(name = "check_str", required = false, defaultValue = "") String checkStr,
        @RequestHeader Map<String, String> map) throws Exception {
        boolean signatureResult = checkSign(map, checkStr);
        log.info("验签结果：{}", signatureResult);
        String decrypt = decrypt(checkStr, encodingAESKey);
        log.info("明文结果：{}", decrypt);
        return decrypt;
    }

    @PostMapping("/")
    @ResponseBody
    public String eventHander(@RequestBody CommonWeHookRequest commonWeHookRequest,
        @RequestHeader Map<String, String> map) throws Exception {
        String data = commonWeHookRequest.getData();
        boolean signatureResult = checkSign(map, data);
        log.info("验签结果：{}", signatureResult);
        String decrypt = decrypt(data, encodingAESKey);

        log.info("事件明文：{}",decrypt);
        return "successfully received callback";
    }

    private boolean checkSign(Map<String, String> map, String checkStr) {
//        String timestamp = map.get("timestamp");
//        String nonce = map.get("nonce");
//        String signature = map.get("signature");
//        //根据已有的 token，结合上述 timestamp、nonce、check_str 参数计算签名，并与 signature 参数对比是否一致，一致表示调用合法
//        String sign = Sha1Util.calSignature(token, timestamp, nonce, checkStr);
//        return signature.equals(sign);
        return true;
    }
}
