package com.tiens.meeting.dubboservice.core.impl;

import com.alibaba.fastjson.JSON;
import com.jtmm.website.api.ImLanguageSourceService;
import com.jtmm.website.api.bo.ImLanguageSourceDTO;
import com.jtmm.website.api.vo.LanguageSourceVO;
import com.tiens.meeting.dubboservice.core.LanguageService;
import common.util.cache.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/23
 * @Version 1.0
 * @Company: tiens
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private final RedissonClient redissonClient;

    @Reference(version = "1.0")
    ImLanguageSourceService imLanguageSourceService;

    /**
     * 查询词条value
     *
     * @param languageId
     * @param languageKey
     * @return
     */
    @Override
    public String getLanguageValue(String languageId, String languageKey) {
        RMap<String, String> languageKeyMap = redissonClient.getMap(CacheKeyUtil.getLanguageKey(languageId));
        String languageValue = languageKeyMap.get(languageKey);
        if (StringUtils.isBlank(languageValue)) {
            ImLanguageSourceDTO imLanguageSourceDTO = new ImLanguageSourceDTO();
            imLanguageSourceDTO.setLanguageId(languageId);
            imLanguageSourceDTO.setVersion("0");
            log.info("词条查询入参：{}", JSON.toJSONString(imLanguageSourceDTO));
            LanguageSourceVO listByCode = imLanguageSourceService.getListByCode(imLanguageSourceDTO);
            Map<String, String> sources = listByCode.getSources();
            log.info("词条查询返回条数：{}", sources.size());

            languageKeyMap.expire(15, TimeUnit.DAYS);
            languageKeyMap.putAll(sources);
            languageValue = sources.getOrDefault(languageKey, "undefined workKey");
        }

        return languageValue;
    }
}
