package com.tiens.meeting.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.List;
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

    public static void main(String[] args) throws InterruptedException {
         TimedCache<String, List<String>> holiday = CacheUtil.newTimedCache(1000 * 3);
        holiday.schedulePrune(500);
        List<String> stringList = holiday.get("hello");
        System.out.println("第一次结果：" + stringList);
        holiday.put("hello", Lists.newArrayList("world"));
        while (true) {
            System.out.println("循环打印" + holiday.get("hello",false));
            Thread.sleep(500);
        }

    }
}
