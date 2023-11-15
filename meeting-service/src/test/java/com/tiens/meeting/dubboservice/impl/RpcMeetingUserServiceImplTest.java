package com.tiens.meeting.dubboservice.impl;

import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.ServiceApplication;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("dev2-server")
class RpcMeetingUserServiceImplTest {

    @Autowired
    RpcMeetingUserService rpcMeetingUserService;

    @Reference(version = "1.0",mock = "com.tiens.meeting.dubboservice.mock.DubboCommonUserServiceMock")
    DubboCommonUserService dubboCommonUserService;

    @Test
    void queryVMUser() {
        CommonResult<VMUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryVMUser("123456","");
        System.out.println(vmUserVOCommonResult);

    }

    @Test
    void addMeetingHostUser() {
        CommonResult commonResult = rpcMeetingUserService.addMeetingHostUser("123456");
        System.out.println(commonResult);
    }

    @Test
    void removeMeetingHostUser() {
        System.out.println(rpcMeetingUserService.removeMeetingHostUser(1723897338165645313L));
    }

    @Test
    void queryMeetingHostUser() {
        System.out.println(rpcMeetingUserService.queryMeetingHostUser("h5v4qv8wl6916xld599q2vwkyrnncb9lfkj7kmh1"));


    }

    @Test
    void queryPage() {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);
        MeetingHostPageDTO meetingHostPageDTO = new MeetingHostPageDTO();
        meetingHostPageDTO.setName("22");
        pageParam.setCondition(meetingHostPageDTO);
        PageResult<MeetingHostUserVO> pageResult = rpcMeetingUserService.queryPage(pageParam);
        System.out.println(pageResult);

    }
}