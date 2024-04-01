package com.tiens.meeting.repository.config;

import cn.hutool.extra.spring.SpringUtil;
import com.tiens.meeting.repository.po.MeetingTimeZoneConfigPO;
import com.tiens.meeting.repository.service.MeetingTimeZoneConfigDaoService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/5/15
 * @Version 1.0
 */
@Component
@Slf4j
@Getter
public class DataCache implements InitializingBean {

    /**
     * 时区
     */
    List<MeetingTimeZoneConfigPO> meetingTimeZoneConfigPOList;

    @Override
    public void afterPropertiesSet() throws Exception {

        meetingTimeZoneConfigPOList = SpringUtil.getBean(MeetingTimeZoneConfigDaoService.class).list();

    }
}
