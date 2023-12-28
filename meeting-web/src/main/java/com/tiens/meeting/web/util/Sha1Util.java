package com.tiens.meeting.web.util;

import java.security.MessageDigest;
import java.util.Arrays;

public class Sha1Util {

    // 生成签名
    public static String calSignature(String token, String timestamp, String nonce, String data) {
        String[] arr = new String[] {token, timestamp, nonce, data};
        Arrays.sort(arr);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
        }
        return getSha1(sb.toString());
    }

    // sha1签名算法
    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }
}

