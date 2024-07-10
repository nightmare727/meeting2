package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tiens.api.dto.*;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.MeetingInfo;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.dubboservice.async.RoomAsyncTaskService;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.job.*;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import common.enums.MeetingResourceHandleEnum;
import common.enums.MeetingUserJoinSourceEnum;
import common.pojo.CommonResult;
import common.util.date.DateUtils;
import lombok.SneakyThrows;
import org.apache.dubbo.config.annotation.Reference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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

    @Autowired
    RoomAsyncTaskService roomAsyncTaskService;

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    HWUserCleanTask hwUserCleanTask;

    @Autowired
    InvalidMeetingCleanTask invalidMeetingCleanTask;

    @Autowired
    RedissonClient redissonClient;

    @Test
    void testRedis() {
        RBucket<String> bucket =
            redissonClient.getBucket("vmmoment-meeting:im-login-meeting-user:0b7891ab4d17458eb8be41332ff1e120");

        System.out.println(bucket.get());

        System.out.println("完成设置");
    }

    @Test
    void getCredential() {
        DateTime now = DateUtil.date();
        System.out.println(now);
    }

    @Test
    void enterMeetingRoomCheck() {

        EnterMeetingRoomCheckDTO enterMeetingRoomCheckDTO = new EnterMeetingRoomCheckDTO();
        enterMeetingRoomCheckDTO.setImUserId("6d3a332e5042431682974e58729cebe9");
        enterMeetingRoomCheckDTO.setMeetRoomCode("961447500");

        System.out.println(rpcMeetingRoomService.enterMeetingRoomCheck(enterMeetingRoomCheckDTO));
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
        meetingRoomContextDTO.setStartTime(DateUtil.parse("2024-07-10 15:00:00"));
        meetingRoomContextDTO.setLength(60);
        meetingRoomContextDTO.setSubject("云会议-文杰测试会议" + RandomUtil.randomInt(100));
        meetingRoomContextDTO.setResourceId(398);
        meetingRoomContextDTO.setResourceType("1");
//        meetingRoomContextDTO.setVmrId();
//        meetingRoomContextDTO.setVmrMode();
        meetingRoomContextDTO.setGuestPwdFlag(false);
        meetingRoomContextDTO.setLevelCode(9);
        meetingRoomContextDTO.setImUserId("a6afdeaaa1ca4100a3f089a0e46a87b7");
        meetingRoomContextDTO.setImUserName("文杰昵称");
        meetingRoomContextDTO.setTimeZoneOffset("GMT+10:30");
        meetingRoomContextDTO.setJoyoCode("1540886");
        meetingRoomContextDTO.setMemberType(1);
        meetingRoomContextDTO.setLanguageId("zh-CN");
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
            rpcMeetingRoomService.getMeetingRoom(1772842609634136066L, "48cd6848a5ca47c883bd38a5c64287dd"));
    }

    @Test
    void cancelMeetingRoom() {
        CancelMeetingRoomDTO cancelMeetingRoomDTO = new CancelMeetingRoomDTO();
        cancelMeetingRoomDTO.setMeetingRoomId(1783042528646344706L);

        cancelMeetingRoomDTO.setImUserId("cb4b8cc1be09409eb108baf982d7e196");

        System.out.println(rpcMeetingRoomService.cancelMeetingRoom(cancelMeetingRoomDTO));
    }

    @Test
    void publicResourceHoldHandle() {
        rpcMeetingRoomServiceImpl.publicResourceHoldHandle(203, MeetingResourceHandleEnum.HOLD_DOWN);
    }

    @Test
    void getFutureAndRunningMeetingRoomList() {
        FutureAndRunningMeetingRoomListGetDTO futureAndRunningMeetingRoomListGetDTO =
            new FutureAndRunningMeetingRoomListGetDTO();
        futureAndRunningMeetingRoomListGetDTO.setFinalUserId("90a2aed7dcaf45c398ccb39dc6a22f2b");
        futureAndRunningMeetingRoomListGetDTO.setTimeZoneOffset("GMT+08:00");

        System.out.println(
            rpcMeetingRoomService.getFutureAndRunningMeetingRoomList(futureAndRunningMeetingRoomListGetDTO));
    }

    @Test
    void getHistoryMeetingRoomList() {
        HistoryMeetingRoomListGetDTO historyMeetingRoomListGetDTO = new HistoryMeetingRoomListGetDTO();
        historyMeetingRoomListGetDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
        historyMeetingRoomListGetDTO.setMonth(3);
        historyMeetingRoomListGetDTO.setFinalUserId("cf2828195d364c6cbf4c9fa83f6abee8");

        System.out.println(rpcMeetingRoomService.getHistoryMeetingRoomList(historyMeetingRoomListGetDTO));
    }

    @Test
    void getAvailableResourcePeriod() {
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO = new AvailableResourcePeriodGetDTO();
        availableResourcePeriodGetDTO.setResourceId(398);
        availableResourcePeriodGetDTO.setImUserId("90a2aed7dcaf45c398ccb39dc6a22f2b");
        availableResourcePeriodGetDTO.setDate(DateUtil.parse("2024-04-12 00:30:51"));
        availableResourcePeriodGetDTO.setTimeZoneOffset("GMT-11:00");
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
    void hwUserCleanTask() {
        hwUserCleanTask.jobHandler();
    }

    @Test
    @SneakyThrows
    void invalidMeetingCleanTask() {
        invalidMeetingCleanTask.jobHandler();
    }

    @Test
    @SneakyThrows
    void hwStopTask() {
        meetingStopTask.jobHandler();
    }

    @Test
    @SneakyThrows
    void getMeetingResourceTypeList() {

        System.out.println(
            rpcMeetingRoomService.getMeetingResourceTypeList("caf3db70e08b496abf51e857f4211fff", 2, "CN", 1));

    }

    @Test
    @SneakyThrows
    void batchInsert() {
        ArrayList<@Nullable MeetingAttendeePO> objects = Lists.newArrayListWithExpectedSize(10000);
        for (int i = 0; i < 10000; i++) {
            MeetingAttendeePO meetingAttendeePO = new MeetingAttendeePO();
            meetingAttendeePO.setMeetingRoomId(12345L);
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

    @Test
    @SneakyThrows
    void doSendMultiPersonsAward() {
        MeetingRoomInfoPO meetingRoomInfoPO = new MeetingRoomInfoPO();
        meetingRoomInfoPO.setOwnerImUserId("cb4b8cc1be09409eb108baf982d7e196");
        meetingRoomInfoPO.setHwMeetingCode("939259254");
        meetingRoomInfoPO.setId(1783042528646344706L);
        roomAsyncTaskService.doSendMultiPersonsAward(meetingRoomInfoPO);

    }

    @Test
    public void updateMeetingRoomStatus() {

        HwEventReq hwEventReq = new HwEventReq();
        hwEventReq.setAppID("11");
        hwEventReq.setTimestamp(1L);
        hwEventReq.setNonce("12");
        hwEventReq.setSignature("123");

        EventInfo eventInfo = new EventInfo();
        eventInfo.setEvent("meeting.end");
        eventInfo.setTimestamp(11L);
        Payload payload = new Payload();
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingID("960538659");
        meetingInfo.setMeetingUUID("3083cfc1bf694f9b8f4a35b34c1d6648");
        meetingInfo.setMeetingCycleSubID("");

        payload.setMeetingInfo(meetingInfo);

        eventInfo.setPayload(payload);

        hwEventReq.setEventInfo(eventInfo);

        CommonResult<String> stringCommonResult = rpcMeetingRoomService.updateMeetingRoomStatus(hwEventReq);
        System.out.println(stringCommonResult);

        LockSupport.park();

    }
}