package com.tiens.meeting.util;

import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Maps;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2024/4/23
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class VmUserUtil {

    public static Map<String, String> getAuthHead(String imUserId) {

        HashMap<@Nullable String, @Nullable String> headers = Maps.newHashMapWithExpectedSize(3);
        long timeStamp = System.currentTimeMillis();
        headers.put("finalUserId", imUserId);
        headers.put("timeStamp", String.valueOf(timeStamp));
        String signBase = imUserId + timeStamp + "VMO";
        String sign = SecureUtil.md5(signBase);
        headers.put("sign", sign);
        return headers;
    }
}
