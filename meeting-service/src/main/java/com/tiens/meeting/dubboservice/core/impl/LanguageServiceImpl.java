package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.jtmm.website.api.ImLanguageSourceService;
import com.jtmm.website.api.bo.ImLanguageSourceDTO;
import com.jtmm.website.api.vo.LanguageSourceWrap;
import com.tiens.meeting.dubboservice.core.LanguageService;
import common.util.cache.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

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
        RBucket<String> languageCache = redissonClient.getBucket(CacheKeyUtil.getLanguageKey(languageId, languageKey));
        String languageValue = languageCache.get();
        if (StringUtils.isBlank(languageValue)) {
            ImLanguageSourceDTO imLanguageSourceDTO = new ImLanguageSourceDTO();
            imLanguageSourceDTO.setLanguageId(languageId);
            imLanguageSourceDTO.setLanguageKey(languageKey);
//            imLanguageSourceDTO.setVersion("0");
            log.info("词条查询入参：{}", JSON.toJSONString(imLanguageSourceDTO));
            LanguageSourceWrap singleForUgc = imLanguageSourceService.getSingleForUgc(imLanguageSourceDTO);
            log.info("词条查询返回数据：{}", singleForUgc);
            if (ObjectUtil.isNotNull(singleForUgc)) {
                languageValue = singleForUgc.getLanguageValue();
                languageCache.set(languageValue);
            } else {
                languageValue = "undefined workKey";
            }
        }

        return languageValue;
    }
}
