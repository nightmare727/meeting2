package com.tiens.meeting.dubboservice.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 * 预约提前30分钟锁定资源
 */
@Component
@Slf4j
public class AppointMeetingTask {

    @XxlJob("AppointMeetingJobHandler")
    public void jobHandler() throws Exception {
        System.out.println("ddd");
    }
}
