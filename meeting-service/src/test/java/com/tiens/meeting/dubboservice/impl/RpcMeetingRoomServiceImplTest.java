package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateUtil;
import com.tiens.api.dto.AvailableResourcePeriodGetDTO;
import com.tiens.api.dto.FreeResourceListDTO;
import com.tiens.api.dto.MeetingRoomCreateDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.meeting.ServiceApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/9
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class RpcMeetingRoomServiceImplTest {

    @Autowired
    RpcMeetingRoomService rpcMeetingRoomService;

    @Test
    void getCredential() {

    }

    @Test
    void enterMeetingRoomCheck() {

    }

    @Test
    void getFreeResourceList() {

        FreeResourceListDTO freeResourceListDTO = new FreeResourceListDTO();
        freeResourceListDTO.setImUserId("123123");
        freeResourceListDTO.setLevelCode(9);
        freeResourceListDTO.setStartTime(new Date());
        freeResourceListDTO.setLength(30);
        freeResourceListDTO.setResourceType(6);

        System.out.println(rpcMeetingRoomService.getFreeResourceList(freeResourceListDTO));

    }

    @Test
    void createMeetingRoom() {
        MeetingRoomCreateDTO meetingRoomCreateDTO = new MeetingRoomCreateDTO();
//        meetingRoomCreateDTO.setMeetingRoomId();
//        meetingRoomCreateDTO.setMeetingCode();
        meetingRoomCreateDTO.setStartTime(DateUtil.parse("2023-12-10 12:00:00"));
        meetingRoomCreateDTO.setLength(60);
        meetingRoomCreateDTO.setSubject("文杰测试会议");
        meetingRoomCreateDTO.setTimeZoneID(56);
        meetingRoomCreateDTO.setResourceId(209);
//        meetingRoomCreateDTO.setVmrId();
//        meetingRoomCreateDTO.setVmrMode();
        meetingRoomCreateDTO.setGuestPwd("123456");
        meetingRoomCreateDTO.setLevelCode(9);
        meetingRoomCreateDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        meetingRoomCreateDTO.setImUserName("文杰昵称");

        System.out.println(rpcMeetingRoomService.createMeetingRoom(meetingRoomCreateDTO));
    }

    @Test
    void updateMeetingRoom() {
    }

    @Test
    void getMeetingRoom() {
    }

    @Test
    void cancelMeetingRoom() {
        System.out.println(rpcMeetingRoomService.cancelMeetingRoom(1733506910402760706L));
    }

    @Test
    void publicResourceHoldHandle() {

    }

    @Test
    void getFutureAndRunningMeetingRoomList() {
    }

    @Test
    void getHistoryMeetingRoomList() {
    }

    @Test
    void getAvailableResourcePeriod() {
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO =new AvailableResourcePeriodGetDTO();
        availableResourcePeriodGetDTO.setResourceId(209);
        availableResourcePeriodGetDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        availableResourcePeriodGetDTO.setDate(new Date());




        System.out.println(rpcMeetingRoomService.getAvailableResourcePeriod(availableResourcePeriodGetDTO));

    }

    @Test
    void updateMeetingRoomStatus() {
    }
}