package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.tiens.api.service.RPCMeetingResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/23
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RPCMeetingResourceServiceImpl implements RPCMeetingResourceService {
    public static void main(String[] args) {
        ZoneId zoneId1 = ZoneId.of("GMT+09:00");
        ZoneId zoneId2 = ZoneId.of("GMT+07:00");
        ZoneId zoneId3 = ZoneId.of("GMT");
        Instant now = Instant.now();
        Instant instant = Instant.ofEpochMilli(new Date().getTime());
        Date date = new Date();

        DateTime dateTime = DateUtil.convertTimeZone(date, zoneId3);
        LocalDateTime of = LocalDateTimeUtil.of(dateTime);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println(dateTimeFormatter.format(of));
//        ZonedDateTime zonedDateTime = now.atZone(zoneId1);
//        System.out.println(zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));


//        System.out.println(zonedDateTime);
    }


}
