package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateUtil;
import com.tiens.api.dto.MeetingApproveDTO;
import com.tiens.api.dto.MeetingApproveOperateDTO;
import com.tiens.api.dto.MeetingApprovePageDTO;
import com.tiens.api.service.MeetingApproveService;
import com.tiens.api.vo.MeetingApproveVO;
import com.tiens.meeting.ServiceApplication;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: 蔚文杰
 * @Date: 2024/6/12
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class MeetingApproveServiceTest {

    @Autowired
    MeetingApproveService meetingApproveService;

    @Test
    void testRun() {
        System.out.println(meetingApproveService);
    }

    @Test
    void saveApprove() {
        for (int i = 0; i < 19; i++) {
            MeetingApproveDTO meetingApproveDTO = new MeetingApproveDTO();
            meetingApproveDTO.setAccId("testAccId"+i);
            meetingApproveDTO.setJoyoCode("testJoyoCode"+i);
            meetingApproveDTO.setName("文杰");
            meetingApproveDTO.setPhoneNum("99999");
            meetingApproveDTO.setEmail("www.qq1314.@qq.com");
//        meetingApproveDTO.setApproveStatus();
//        meetingApproveDTO.setApproveRemark();
            meetingApproveDTO.setResourceArea(1);
            meetingApproveDTO.setApplyPersonNum(2000);
            meetingApproveDTO.setSubCompanyCode("subCode1");
            meetingApproveDTO.setUseTo("我要开会");
            meetingApproveDTO.setStartTime(DateUtil.parse("2024-07-04 02:00:00"));
            meetingApproveDTO.setTimeZoneOffset("GMT+08:00");
            meetingApproveDTO.setDuration(60);
            meetingApproveDTO.setMeetingTopic("我要开会的主题");

            System.out.println(meetingApproveService.saveApprove(meetingApproveDTO));
        }


    }

    @Test
    void getApproveList() {
        PageParam<MeetingApprovePageDTO> meetingApprovePageDTOPageParam = new PageParam<>();
        meetingApprovePageDTOPageParam.setPageNum(2);
        MeetingApprovePageDTO condition = new MeetingApprovePageDTO();

        meetingApprovePageDTOPageParam.setCondition(condition);
        CommonResult<PageResult<MeetingApproveVO>> approveList =
            meetingApproveService.getApproveList(meetingApprovePageDTOPageParam);

        System.out.println(approveList);

    }

    @Test
    void approveOperate() {

        MeetingApproveOperateDTO meetingApproveOperateDTO = new MeetingApproveOperateDTO();
        meetingApproveOperateDTO.setId(20);
        meetingApproveOperateDTO.setApproveStatus(3);
        meetingApproveOperateDTO.setApproveRemark("我不同意这门亲事");

        System.out.println(meetingApproveService.approveOperate(meetingApproveOperateDTO));


    }
}