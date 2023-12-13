package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.tiens.api.dto.AvailableResourcePeriodGetDTO;
import com.tiens.api.dto.CancelMeetingRoomDTO;
import com.tiens.api.dto.FreeResourceListDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.ResourceTypeVO;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.dubboservice.job.HWResourceTask;
import common.enums.MeetingResourceHandleEnum;
import common.pojo.CommonResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

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
    @Autowired
    RpcMeetingRoomServiceImpl rpcMeetingRoomServiceImpl;

    @Autowired
    HWResourceTask hwResourceTask;

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
        freeResourceListDTO.setResourceType("6");

        System.out.println(rpcMeetingRoomService.getFreeResourceList(freeResourceListDTO));

    }

    @Test
    void createMeetingRoom() {
        MeetingRoomContextDTO meetingRoomContextDTO = new MeetingRoomContextDTO();
//        meetingRoomContextDTO.setMeetingRoomId();
//        meetingRoomContextDTO.setMeetingCode();
        meetingRoomContextDTO.setStartTime(DateUtil.parse("2023-12-22 12:00:00"));
        meetingRoomContextDTO.setLength(200);
        meetingRoomContextDTO.setSubject("网络研讨会-文杰测试会议" + RandomUtil.randomInt(100));
        meetingRoomContextDTO.setTimeZoneID(56);
        meetingRoomContextDTO.setResourceId(203);
//        meetingRoomContextDTO.setVmrId();
//        meetingRoomContextDTO.setVmrMode();
        meetingRoomContextDTO.setGuestPwdFlag(false);
        meetingRoomContextDTO.setLevelCode(9);
        meetingRoomContextDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        meetingRoomContextDTO.setImUserName("文杰昵称");

        System.out.println(rpcMeetingRoomService.createMeetingRoom(meetingRoomContextDTO));
    }

    @Test
    void updateMeetingRoom() {
        MeetingRoomContextDTO meetingRoomContextDTO = new MeetingRoomContextDTO();
        meetingRoomContextDTO.setMeetingRoomId(1734014601566973954L);
//        meetingRoomContextDTO.setMeetingRoomId();
//        meetingRoomContextDTO.setMeetingCode();
        meetingRoomContextDTO.setStartTime(DateUtil.parse("2023-12-22 12:00:00"));
        meetingRoomContextDTO.setLength(200);
        meetingRoomContextDTO.setSubject("网络研讨会-文杰测试会议" + RandomUtil.randomInt(100));
        meetingRoomContextDTO.setTimeZoneID(56);
        meetingRoomContextDTO.setResourceId(209);
//        meetingRoomContextDTO.setVmrId();
//        meetingRoomContextDTO.setVmrMode();
        meetingRoomContextDTO.setGuestPwdFlag(false);
        meetingRoomContextDTO.setLevelCode(9);
        meetingRoomContextDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        meetingRoomContextDTO.setImUserName("文杰昵称");
        System.out.println(rpcMeetingRoomService.updateMeetingRoom(meetingRoomContextDTO));

    }

    @Test
    void getMeetingRoom() {
        System.out.println(
            rpcMeetingRoomService.getMeetingRoom(1734014601566973954L, "48cd6848a5ca47c883bd38a5c64287dd"));
    }

    @Test
    void cancelMeetingRoom() {
        CancelMeetingRoomDTO cancelMeetingRoomDTO = new CancelMeetingRoomDTO();
        cancelMeetingRoomDTO.setMeetingRoomId(1734014601566973954L);
        System.out.println(rpcMeetingRoomService.cancelMeetingRoom(cancelMeetingRoomDTO));
    }

    @Test
    void publicResourceHoldHandle() {
        rpcMeetingRoomServiceImpl.publicResourceHoldHandle(203, MeetingResourceHandleEnum.HOLD_DOWN);
    }

    @Test
    void getFutureAndRunningMeetingRoomList() {
        System.out.println(
            rpcMeetingRoomService.getFutureAndRunningMeetingRoomList("48cd6848a5ca47c883bd38a5c64287dd"));
    }

    @Test
    void getHistoryMeetingRoomList() {
        System.out.println(rpcMeetingRoomService.getHistoryMeetingRoomList("48cd6848a5ca47c883bd38a5c64287dd", 12));
    }

    @Test
    void getAvailableResourcePeriod() {
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO = new AvailableResourcePeriodGetDTO();
        availableResourcePeriodGetDTO.setResourceId(209);
        availableResourcePeriodGetDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        availableResourcePeriodGetDTO.setDate(new Date());

        System.out.println(rpcMeetingRoomService.getAvailableResourcePeriod(availableResourcePeriodGetDTO));

    }

    @Test
    void getMeetingRoomRecordList() {
        System.out.println(rpcMeetingRoomService.getMeetingRoomRecordList(1733506910402760706L));
    }

    @Test
    @SneakyThrows
    void hwResourceTask() {
        hwResourceTask.jobHandler();
    }

    @Test
    @SneakyThrows
    void getMeetingResourceTypeList() {

        System.out.println(rpcMeetingRoomService.getMeetingResourceTypeList("123", 7));


    }
}