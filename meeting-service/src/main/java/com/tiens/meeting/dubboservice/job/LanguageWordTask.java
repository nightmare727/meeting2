package com.tiens.meeting.dubboservice.job;

import com.alibaba.fastjson.JSON;
import com.jtmm.website.api.ImLanguageSourceService;
import com.jtmm.website.api.bo.ImLanguageSourceDTO;
import com.jtmm.website.api.vo.LanguageSourceVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 *
 *     词条定时刷新
 */
@Component
@Slf4j
public class LanguageWordTask {

    @Autowired
    private RedissonClient redissonClient;

    @Reference(version = "1.0")
    ImLanguageSourceService imLanguageSourceService;

    @Autowired
    MeetingConfig meetingConfig;

    @XxlJob("LanguageWordJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog
    public void jobHandler() throws Exception {
        ImLanguageSourceDTO imLanguageSourceDTO = new ImLanguageSourceDTO();
        imLanguageSourceDTO.setLanguageId("");
        imLanguageSourceDTO.setVersion("0");
        log.info("词条查询入参：{}", JSON.toJSONString(imLanguageSourceDTO));
        LanguageSourceVO listByCode = imLanguageSourceService.getListByCode(imLanguageSourceDTO);
        Map<String, String> sources = listByCode.getSources();
        log.info("词条查询返回条数：{}", sources.size());
    }
}
