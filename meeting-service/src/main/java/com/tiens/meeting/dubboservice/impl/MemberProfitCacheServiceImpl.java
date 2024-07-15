package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.api.service.MemberProfitCacheService;
import com.tiens.meeting.repository.po.MeetingProfitCommonConfigPO;
import com.tiens.meeting.repository.service.MeetingProfitCommonConfigDaoService;
import common.constants.CommonProfitConfigConstants;
import common.util.cache.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MemberProfitCacheServiceImpl implements MemberProfitCacheService {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    MeetingProfitCommonConfigDaoService meetingProfitCommonConfigDaoService;

    /**
     * 刷新会员权益缓存
     */
    @Override
    public void refreshMemberProfitCache() {
        log.info("【刷新会员权益缓存】开始~~");
        List<MeetingProfitCommonConfigPO> list = meetingProfitCommonConfigDaoService.list();
        Map<String, String> keyValueMap = list.stream().collect(
            Collectors.toMap(MeetingProfitCommonConfigPO::getConfigKey, MeetingProfitCommonConfigPO::getConfigValue));

        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());
        map.putAll(keyValueMap);
        log.info("【刷新会员权益缓存】结束，完成设置数据：{}", JSON.toJSONString(list));
    }

    /**
     * 查询首页cms配置
     *
     * @return
     */
    @Override
    public Boolean getCmsShowEnabled() {
        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());
        String result = map.get(CommonProfitConfigConstants.CMS_SHOW_FLAG);
        return StrUtil.isNotBlank(result) && "1".equals(result);
    }

    /**
     * 查询会员权益是否生效
     *
     * @return
     */
    @Override
    public Boolean getMemberProfitEnabled() {
        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());
        String result = map.get(CommonProfitConfigConstants.MEMBER_PROFIT_FLAG);
        return StrUtil.isNotBlank(result) && "1".equals(result);
    }
}
