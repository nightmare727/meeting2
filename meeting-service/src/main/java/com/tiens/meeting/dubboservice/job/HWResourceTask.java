package com.tiens.meeting.dubboservice.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HWResourceTask {

    //@XxlJob("AppointMeetingJobHandler")
    public void jobHandler() throws Exception {
        System.out.println("ddd");
    }
}
