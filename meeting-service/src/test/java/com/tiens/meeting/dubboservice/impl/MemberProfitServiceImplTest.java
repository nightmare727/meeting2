package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateUtil;
import com.tiens.api.dto.*;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.CommonProfitConfigQueryVO;
import com.tiens.api.vo.MeetingUserProfitVO;
import com.tiens.meeting.ServiceApplication;
import common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/6
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class MemberProfitServiceImplTest {

    @Autowired
    MemberProfitService memberProfitService;

    @Test
    void checkProfit() {
    }

    @Test
    void getCmsShow() {
        CmsShowGetDTO cmsShowGetDTO = new CmsShowGetDTO();
        cmsShowGetDTO.setDeviceType(1);
        cmsShowGetDTO.setNationId("CN");
        cmsShowGetDTO.setLanguageId("zh-CN");
        System.out.println(memberProfitService.getCmsShow(cmsShowGetDTO));
    }

    @Test
    void pushOrder() {
        PushOrderDTO pushOrderDTO = new PushOrderDTO();
        pushOrderDTO.setNationId("CN");
//        pushOrderDTO.setAccId("12345");
        pushOrderDTO.setJoyoCode("1540886");
        pushOrderDTO.setOrderNo("20240705103306096469");
        pushOrderDTO.setSkuId("11021765");
        pushOrderDTO.setOrderStatus(1);
        pushOrderDTO.setPaidVmAmount(new BigDecimal("60000"));
//        pushOrderDTO.setPaidRealAmount();
        pushOrderDTO.setResourceType(1);
        pushOrderDTO.setDuration(60);

        System.out.println(memberProfitService.pushOrder(pushOrderDTO));
    }

    @Test
    void getBlackUser() {
        System.out.println(memberProfitService.getBlackUser("5e5829e54e9045e5907bafd6ad89daf1"));
    }

    @Test
    void getUserProfit() {

        String finalUserId = "e1b81792d04f4b96b0c95ceb51be2da2";
        Integer memberType = 10;
        CommonResult<MeetingUserProfitVO> userProfit = memberProfitService.getUserProfit(finalUserId, memberType);

        System.out.println(userProfit);

    }

    @Test
    void queryUserProfitConfig() {
        System.out.println(memberProfitService.queryUserProfitConfig());
    }

    @Test
    void modUserMemberProfit() {

        UserMemberProfitModifyEntity userMemberProfitModifyEntity = new UserMemberProfitModifyEntity();
        userMemberProfitModifyEntity.setAccId("e0e909954bcb48cab8ef0654892a5b87");
        userMemberProfitModifyEntity.setGetType(1);

        memberProfitService.modUserMemberProfit(userMemberProfitModifyEntity);

    }

    @Test
    void saveCommonProfitConfig() {

        CommonProfitConfigSaveDTO commonProfitConfigSaveDTO = new CommonProfitConfigSaveDTO();
        commonProfitConfigSaveDTO.setCmsShowFlag("0");
        commonProfitConfigSaveDTO.setMemberProfitFlag("0");

        System.out.println(memberProfitService.saveCommonProfitConfig(commonProfitConfigSaveDTO));
    }

    @Test
    void queryCommonProfitConfig() {
        CommonResult<CommonProfitConfigQueryVO> commonProfitConfigQueryVOCommonResult =
            memberProfitService.queryCommonProfitConfig();

        System.out.println(commonProfitConfigQueryVOCommonResult);
    }

    @Test
    void buyMeetingProfit() {
        BuyMeetingProfitDTO buyMeetingProfitDTO = new BuyMeetingProfitDTO();
        buyMeetingProfitDTO.setResourceType("2");
        buyMeetingProfitDTO.setFinalUserId("74762bd1739c44dba971ba848ed2bacd");
        buyMeetingProfitDTO.setJoyoCode("67892185");
        buyMeetingProfitDTO.setNationId("CN");
        buyMeetingProfitDTO.setStartTime(DateUtil.parse("2024-07-16 09:30:00"));
        buyMeetingProfitDTO.setTimeZoneOffset("GMT+08:00");
        buyMeetingProfitDTO.setLength(60);
        buyMeetingProfitDTO.setLeadTime(30);

        memberProfitService.buyMeetingProfit(buyMeetingProfitDTO);
    }
}