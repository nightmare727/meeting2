package com.tiens.meeting.dubboservice.listener;

import com.tiens.api.service.MemberProfitCacheService;
import com.tiens.meeting.repository.po.MeetingMemeberProfitConfigPO;
import com.tiens.meeting.repository.po.MeetingProfitProductListPO;
import com.tiens.meeting.repository.service.MeetingMemeberProfitConfigDaoService;
import com.tiens.meeting.repository.service.MeetingProfitProductListDaoService;
import common.util.cache.CacheKeyUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
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

    @Autowired
    private MemberProfitCacheService memberProfitCacheService;

    @Order
    @EventListener({ApplicationStartedEvent.class})
    public void initConfig() {
        //1、初始化会员权益配置
        initMeetingMemberProfitConfig();
        //2、初始化权益商品配置
        initMemberProfitProduct();
        //
        memberProfitCacheService.refreshMemberProfitCache();
        log.info("完成数据初始化");
    }

    void initMeetingMemberProfitConfig() {
        List<MeetingMemeberProfitConfigPO> list = meetingMemeberProfitConfigDaoService.list();
        RMap<Integer, MeetingMemeberProfitConfigPO> map =
            redissonClient.getMap(CacheKeyUtil.getMemberProfitConfigKey());
        Map<Integer, MeetingMemeberProfitConfigPO> collect =
            list.stream().collect(Collectors.toMap(MeetingMemeberProfitConfigPO::getMemberType, Function.identity()));
        map.putAll(collect);
    }

    void initMemberProfitProduct() {
        List<MeetingProfitProductListPO> list = meetingProfitProductListDaoService.list();
        RMap<Integer, MeetingProfitProductListPO> map = redissonClient.getMap(CacheKeyUtil.getProfitProductListKey());

        Map<Integer, MeetingProfitProductListPO> collect =
            list.stream().collect(Collectors.toMap(MeetingProfitProductListPO::getResourceType, Function.identity()));
        map.putAll(collect);
        memberProfitCacheService.refreshMemberProfitCache();
        log.info("完成数据初始化");
    }

}
