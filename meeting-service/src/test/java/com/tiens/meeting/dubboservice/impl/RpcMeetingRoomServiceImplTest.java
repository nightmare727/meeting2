package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tiens.api.dto.AvailableResourcePeriodGetDTO;
import com.tiens.api.dto.CancelMeetingRoomDTO;
import com.tiens.api.dto.FreeResourceListDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.dubboservice.job.AppointMeetingTask;
import com.tiens.meeting.dubboservice.job.HWResourceTask;
import com.tiens.meeting.dubboservice.job.MeetingStopTask;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import common.enums.MeetingResourceHandleEnum;
import common.enums.MeetingUserJoinSourceEnum;
import lombok.SneakyThrows;
import org.apache.dubbo.config.annotation.Reference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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

    @Reference
    RpcMeetingRoomService rpcMeetingRoomService;
    @Autowired
    RpcMeetingRoomServiceImpl rpcMeetingRoomServiceImpl;

    @Autowired
    HWResourceTask hwResourceTask;
    @Autowired
    AppointMeetingTask appointMeetingTask;
    @Autowired
    MeetingStopTask meetingStopTask;

    @Autowired
    MeetingAttendeeDaoService meetingAttendeeDaoService;

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
        freeResourceListDTO.setStartTime(DateUtil.parse("2023-12-27 17:00:00"));
        freeResourceListDTO.setLength(60);
        freeResourceListDTO.setResourceType("2");

        System.out.println(rpcMeetingRoomService.getFreeResourceList(freeResourceListDTO));

    }

    @Test
    void createMeetingRoom() throws Exception {
        MeetingRoomContextDTO meetingRoomContextDTO = new MeetingRoomContextDTO();
//        meetingRoomContextDTO.setMeetingRoomId();
//        meetingRoomContextDTO.setMeetingCode();
        meetingRoomContextDTO.setStartTime(null);
        meetingRoomContextDTO.setLength(60);
        meetingRoomContextDTO.setSubject("云会议-文杰测试会议" + RandomUtil.randomInt(100));
        meetingRoomContextDTO.setResourceId(389);
        meetingRoomContextDTO.setResourceType("2");
//        meetingRoomContextDTO.setVmrId();
//        meetingRoomContextDTO.setVmrMode();
        meetingRoomContextDTO.setGuestPwdFlag(false);
        meetingRoomContextDTO.setLevelCode(9);
        meetingRoomContextDTO.setImUserId("7a4037c1a8234ba286647f31aadfc4f1");
        meetingRoomContextDTO.setImUserName("文杰昵称");
//        List<MeetingAttendeeDTO> meetingAttendeeDTOS = Lists.newArrayList();
//        MeetingAttendeeDTO meetingAttendeeDTO = new MeetingAttendeeDTO();
//        meetingAttendeeDTO.setAttendeeUserId("cb4b8cc1be09409eb108baf982d7e196");
//        meetingAttendeeDTO.setAttendeeUserName("wenjie");
//        meetingAttendeeDTO.setAttendeeUserHeadUrl("wenjie_url");
//
//        meetingAttendeeDTOS.add(meetingAttendeeDTO);
//        meetingRoomContextDTO.setRemark("备注");
//        meetingRoomContextDTO.setAttendees(meetingAttendeeDTOS);
//        meetingRoomContextDTO.setRemark("测试备注");
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
            rpcMeetingRoomService.getMeetingRoom(1767443276215427073L, "48cd6848a5ca47c883bd38a5c64287dd"));
    }

    @Test
    void cancelMeetingRoom() {
        CancelMeetingRoomDTO cancelMeetingRoomDTO = new CancelMeetingRoomDTO();
        cancelMeetingRoomDTO.setMeetingRoomId(1751846457861304321L);

        cancelMeetingRoomDTO.setImUserId("8ff6a6c6bec64761a72fa23df6e72a0d");

        System.out.println(rpcMeetingRoomService.cancelMeetingRoom(cancelMeetingRoomDTO));
    }

    @Test
    void publicResourceHoldHandle() {
        rpcMeetingRoomServiceImpl.publicResourceHoldHandle(203, MeetingResourceHandleEnum.HOLD_DOWN);
    }

    @Test
    void getFutureAndRunningMeetingRoomList() {
        System.out.println(
            rpcMeetingRoomService.getFutureAndRunningMeetingRoomList("a19367142fdd4278bb5009aae35ee7af", ""));
    }

    @Test
    void getHistoryMeetingRoomList() {
        System.out.println(rpcMeetingRoomService.getHistoryMeetingRoomList("1", 12));
    }

    @Test
    void getAvailableResourcePeriod() {
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO = new AvailableResourcePeriodGetDTO();
        availableResourcePeriodGetDTO.setResourceId(308);
        availableResourcePeriodGetDTO.setImUserId("48cd6848a5ca47c883bd38a5c64287dd");
        availableResourcePeriodGetDTO.setDate(DateUtil.parse("2023-12-28 10:30:51"));

        System.out.println(
            JSON.toJSONString(rpcMeetingRoomService.getAvailableResourcePeriod(availableResourcePeriodGetDTO)));

    }

    @Test
    void getMeetingRoomRecordList() {
        System.out.println(rpcMeetingRoomService.getMeetingRoomRecordList(1738002691414646786L));
    }

    @Test
    void getAllMeetingResourceList() {
        System.out.println(rpcMeetingRoomService.getAllMeetingResourceList("9f450708cf794c3889cc06d2e6ec1029-50-2"));
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

        System.out.println(rpcMeetingRoomService.getMeetingResourceTypeList("caf3db70e08b496abf51e857f4211fff", 2));

    }

    @Test
    @SneakyThrows
    void batchInsert() {
        ArrayList<@Nullable MeetingAttendeePO> objects = Lists.newArrayListWithExpectedSize(1500);
        for (int i = 0; i < 1500; i++) {
            MeetingAttendeePO meetingAttendeePO = new MeetingAttendeePO();
            meetingAttendeePO.setMeetingRoomId(12222222311231313L);
            meetingAttendeePO.setAttendeeUserId(RandomUtil.randomNumbers(32));
            meetingAttendeePO.setAttendeeUserName("张三");
            meetingAttendeePO.setSource(MeetingUserJoinSourceEnum.APPOINT.getCode());
            objects.add(meetingAttendeePO);
        }
        StopWatch stopWatch = DateUtil.createStopWatch();
        stopWatch.start();
        boolean b = meetingAttendeeDaoService.saveBatch(objects);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}