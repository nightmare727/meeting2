package com.tiens.meeting.dubboservice.impl;

import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.repository.mapper.MeetingResouceMapper;
import common.pojo.CommonResult;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: ly
 * @Date: 2023/11/28
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class ResouceDaoServiceImplTest {

   @Autowired
   private MeetingResouceMapper meetingResouceMapper;
   @Test
    void update(){
      int update = meetingResouceMapper.update("2");
      System.out.println(update);
   }

}