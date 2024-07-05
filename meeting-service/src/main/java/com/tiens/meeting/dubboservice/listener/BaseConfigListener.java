package com.tiens.meeting.dubboservice.listener;

import com.tiens.meeting.repository.po.MeetingMemeberProfitConfigPO;
import com.tiens.meeting.repository.service.MeetingMemeberProfitConfigDaoService;
import common.util.cache.CacheKeyUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/5
 * @Version 1.0
 * @Company: tiens
 */
@Configuration
@AllArgsConstructor
@Slf4j
public class BaseConfigListener implements Serializable {
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    MeetingMemeberProfitConfigDaoService meetingMemeberProfitConfigDaoService;

    @Order
    @EventListener({WebServerInitializedEvent.class})
    public void initConfig() {
        List<MeetingMemeberProfitConfigPO> list = meetingMemeberProfitConfigDaoService.list();
        RMap<Integer, MeetingMemeberProfitConfigPO> map =
            redissonClient.getMap(CacheKeyUtil.getMemberProfitConfigKey());
        Map<Integer, MeetingMemeberProfitConfigPO> collect =
            list.stream().collect(Collectors.toMap(MeetingMemeberProfitConfigPO::getMemberType, Function.identity()));
        map.putAll(collect);

        log.info("完成数据初始化");
    }

}
