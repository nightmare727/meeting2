package com.tiens.meeting.dubboservice.impl;

import com.tiens.api.dto.FreeResourceListDTO;
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
    }

    @Test
    void updateMeetingRoom() {
    }

    @Test
    void getMeetingRoom() {
    }

    @Test
    void cancelMeetingRoom() {
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
    }

    @Test
    void updateMeetingRoomStatus() {
    }
}