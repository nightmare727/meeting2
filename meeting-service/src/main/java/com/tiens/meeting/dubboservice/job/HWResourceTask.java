package com.tiens.meeting.dubboservice.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 * 华为资源同步
 */
@Component
@Slf4j
public class HWResourceTask {

    @XxlJob("HWResourceJobHandler")
    public void jobHandler() throws Exception {
        System.out.println("ddd");
    }
}
