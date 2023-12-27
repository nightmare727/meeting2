package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.tiens.api.dto.AvailableResourcePeriodGetDTO;
import com.tiens.api.dto.CancelMeetingRoomDTO;
import com.tiens.api.dto.FreeResourceListDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.dubboservice.job.AppointMeetingTask;
import com.tiens.meeting.dubboservice.job.HWResourceTask;
import com.tiens.meeting.dubboservice.job.MeetingStopTask;
import common.enums.MeetingResourceHandleEnum;
import lombok.SneakyThrows;
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
    @Autowired
    RpcMeetingRoomServiceImpl rpcMeetingRoomServiceImpl;

    @Autowired
    HWResourceTask hwResourceTask;
    @Autowired
    AppointMeetingTask appointMeetingTask;
    @Autowired
    MeetingStopTask meetingStopTask;

    @Test
    void getCredential() {

    }

    @Test
    void enterMeetingRoomCheck() {

    }

    @Test
    void getFreeResourceList() {

        FreeResourceListDTO freeResourceListDTO = new FreeResourceListDTO();
        freeResourceListDTO.setImUserId("9f450708cf794c3889cc06d2e6ec1029");
        freeResourceListDTO.setLevelCode(9);
        freeResourceListDTO.setStartTime(DateUtil.parse("2023-12-27 18:00:00"));
        freeResourceListDTO.setLength(60);
        freeResourceListDTO.setResourceType("2");

        System.out.println(rpcMeetingRoomService.getFreeResourceList(freeResourceListDTO));

    }

    @Test
    void createMeetingRoom() throws Exception {
        MeetingRoomContextDTO meetingRoomContextDTO = new MeetingRoomContextDTO();
//        meetingRoomContextDTO.setMeetingRoomId();
//        meetingRoomContextDTO.setMeetingCode();
        meetingRoomContextDTO.setStartTime(DateUtil.parse("2023-12-30 11:30:00"));
        meetingRoomContextDTO.setLength(240);
        meetingRoomContextDTO.setSubject("云会议-文杰测试会议" + RandomUtil.randomInt(100));
        meetingRoomContextDTO.setResourceId(260);
        meetingRoomContextDTO.setResourceType("1");
//        meetingRoomContextDTO.setVmrId();
//        meetingRoomContextDTO.setVmrMode();
        meetingRoomContextDTO.setGuestPwdFlag(false);
        meetingRoomContextDTO.setLevelCode(9);
        meetingRoomContextDTO.setImUserId("7a4037c1a8234ba286647f31aadfc4f1");
        meetingRoomContextDTO.setImUserName("文杰昵称");

        System.out.println(rpcMeetingRoomService.createMeetingRoom(meetingRoomContextDTO));
    }

    @Test
    void updateMeetingRoom() {
        MeetingRoomContextDTO meetingRoomContextDTO = new MeetingRoomContextDTO();
        meetingRoomContextDTO.setMeetingRoomId(1734014601566973954L);
//        meetingRoomContextDTO.setMeetingRoomId();
//        meetingRoomContextDTO.setMeetingCode();
//        meetingRoomContextDTO.setStartTime(DateUtil.parse("2023-12-22 12:00:00"));
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
            rpcMeetingRoomService.getMeetingRoom(1738031692086599682L, "48cd6848a5ca47c883bd38a5c64287dd"));
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
            rpcMeetingRoomService.getFutureAndRunningMeetingRoomList("9f450708cf794c3889cc06d2e6ec1029"));
    }

    @Test
    void getHistoryMeetingRoomList() {
        System.out.println(rpcMeetingRoomService.getHistoryMeetingRoomList("39b593338f584b128381170e4c480c6f", 12));
    }

    @Test
    void getAvailableResourcePeriod() {
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO = new AvailableResourcePeriodGetDTO();
        availableResourcePeriodGetDTO.setResourceId(289);
        availableResourcePeriodGetDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        availableResourcePeriodGetDTO.setDate(DateUtil.parse("2023-12-28 11:00:00"));

        System.out.println(rpcMeetingRoomService.getAvailableResourcePeriod(availableResourcePeriodGetDTO));

    }

    @Test
    void getMeetingRoomRecordList() {
        System.out.println(rpcMeetingRoomService.getMeetingRoomRecordList(1738002691414646786L));
    }

    @Test
    @SneakyThrows
    void hwResourceTask() {
        hwResourceTask.jobHandler();
    }

    @Test
    @SneakyThrows
    void hwAPPointTask() {
        appointMeetingTask.jobHandler();
    }
    @Test
    @SneakyThrows
    void hwStopTask() {
        meetingStopTask.jobHandler();
    }

    @Test
    @SneakyThrows
    void getMeetingResourceTypeList() {

        System.out.println(rpcMeetingRoomService.getMeetingResourceTypeList("123", 7));

    }
}