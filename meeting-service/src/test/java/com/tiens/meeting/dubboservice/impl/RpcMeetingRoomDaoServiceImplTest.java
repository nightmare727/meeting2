package com.tiens.meeting.dubboservice.impl;

import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.meeting.ServiceApplication;
import common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("dev2-server")
class RpcMeetingRoomDaoServiceImplTest {

    @Autowired
    RpcMeetingRoomService rpcMeetingRoomService;
    @Test
    void getCredential() {
        CommonResult<VMMeetingCredentialVO> commonResult =
            rpcMeetingRoomService.getCredential("h5v4qv8wl6916xld599q2vwkyrnncb9lfkj7kmh1");
        System.out.println(commonResult);
    }
}