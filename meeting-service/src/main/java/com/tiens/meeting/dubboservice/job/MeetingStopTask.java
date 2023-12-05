package com.tiens.meeting.dubboservice.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 *
 * 会议提前30分钟
 */
@Component
@Slf4j
public class MeetingStopTask  {

    @XxlJob("MeetingStopJobHandler")
    public void jobHandler() throws Exception {
        System.out.println("ddd");
    }
}
