package com.tiens.meeting.dubboservice.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResourceTypeVO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
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
import com.tiens.meeting.ServiceApplication;

import java.util.List;

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

    @Reference(version = "1.0")
    DubboCommonUserService dubboCommonUserService;

    @Test
    void meetingClient(){
        MeetingClient bean = SpringUtil.getBean(MeetingClient.class);
        MeetingClient bean1 = SpringUtil.getBean(MeetingClient.class);
        MeetingClient bean2 = SpringUtil.getBean(MeetingClient.class);
        System.out.println(bean);
        System.out.println(bean1);
        System.out.println(bean2);
    }

    @Test
    void queryVMUser() {
        CommonResult<VMUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryVMUser("","7ed71cdb710f4414b6f494c64b473906");
        System.out.println(vmUserVOCommonResult);
    }

    @Test
    void addMeetingHostUser() {
        CommonResult commonResult = rpcMeetingUserService.addMeetingHostUser("67891601", 6);
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

        pageResult.getList().forEach(meetingHostUserVO -> {
            System.out.println(meetingHostUserVO);
        });
    }
    @Test
    void queryResourceTypes() {
        CommonResult<List<MeetingResourceTypeVO>> listCommonResult = rpcMeetingUserService.queryResourceTypes(1);
        List<MeetingResourceTypeVO> data = listCommonResult.getData();
        data.forEach(meetingResourceTypeVO -> {
            System.out.println(meetingResourceTypeVO);
        });
    }
}