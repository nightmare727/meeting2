package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingRoomCreateDTO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens 云会议室
 */
@Service
@Slf4j
public class CloudMeetingRoomHandler extends HwMeetingRoomHandler {
    /**
     * 创建华为会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    @Override
    public MeetingRoomModel createMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO) {
        //将开始时间转化成UTC时间

        Date startTime = meetingRoomCreateDTO.getStartTime();
        String startTimeStr = "";
        if (ObjectUtil.isNotNull(startTime)) {
            //非空表示为预约会议
            ZoneId zoneId3 = ZoneId.of("GMT");
            DateTime dateTime = DateUtil.convertTimeZone(startTime, zoneId3);
            LocalDateTime of = LocalDateTimeUtil.of(dateTime);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            startTimeStr = dateTimeFormatter.format(of);
        }
        //分配云会议资源
        associateVmr(meetingRoomCreateDTO.getImUserId(), Collections.singletonList(meetingRoomCreateDTO.getVmrId()));

        MeetingClient userMeetingClient = getUserMeetingClient(meetingRoomCreateDTO.getImUserId());
        CreateMeetingRequest request = new CreateMeetingRequest();
        RestScheduleConfDTO body = new RestScheduleConfDTO();
        RestConfConfigDTO confConfigInfobody = new RestConfConfigDTO();
        confConfigInfobody.withIsGuestFreePwd(false).withCallInRestriction(2).withVmrIDType(1)
            //自动延时30分钟
            .withProlongLength(30).withGuestPwd(meetingRoomCreateDTO.getGuestPwd()).withEnableWaitingRoom(true);
        body.withVmrID(meetingRoomCreateDTO.getVmrId());
        body.withVmrFlag(1);
        body.withConfConfigInfo(confConfigInfobody);
        //禁用录播
        body.withRecordType(0);
        //固定时区GMT+8
        body.withTimeZoneID(String.valueOf(meetingRoomCreateDTO.getTimeZoneID()));
        body.withLanguage("zh-CN");
        body.withEncryptMode(2);
        body.withIsAutoRecord(1);
        body.withMediaTypes("HDVideo");
        body.withSubject(meetingRoomCreateDTO.getSubject());
        body.withLength(meetingRoomCreateDTO.getLength());
        //会议开始时间（UTC时间）。格式：yyyy-MM-dd HH:mm。 > * 创建预约会议时，如果没有指定开始时间或填空串，则表示会议马上开始 > * 时间是UTC时间，即0时区的时间
        body.withStartTime(startTimeStr);
        request.withBody(body);
        CreateMeetingResponse response = userMeetingClient.createMeeting(request);
        log.info("创建云会议结果响应：{}", response);
        if (!ObjectUtil.isEmpty(startTimeStr)) {
            //为预约会议，预约完成后需要回收资源
            disassociateVmr(meetingRoomCreateDTO.getImUserId(),
                Collections.singletonList(meetingRoomCreateDTO.getVmrId()));
        }
        List<ConferenceInfo> body1 = response.getBody();
        ConferenceInfo conferenceInfo = body1.get(0);
        //会议id
        String conferenceID = conferenceInfo.getConferenceID();
        //云会议室会议ID或个人会议ID。如果“vmrFlag”为“1”，则该字段不为空。
        String vmrID = conferenceInfo.getVmrID();
        //会议的UUID-只有创建立即开始的会议才返回UUID，如果是预约未来的会议，不会返回UUID
        String confUUID = conferenceInfo.getConfUUID();
        String conferenceState = conferenceInfo.getConferenceState();

        return new MeetingRoomModel(confUUID, conferenceID, conferenceState);
    }

    /**
     * 修改华为会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    @Override
    public void updateMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO) {
        Date startTime = meetingRoomCreateDTO.getStartTime();
        boolean immediatelyFlag;
        if (immediatelyFlag = ObjectUtil.isNull(startTime)) {
            startTime = DateUtil.date();
        }
        ZoneId zoneId3 = ZoneId.of("GMT");
        DateTime dateTime = DateUtil.convertTimeZone(startTime, zoneId3);
        LocalDateTime of = LocalDateTimeUtil.of(dateTime);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startTimeStr = dateTimeFormatter.format(of);

        //分配云会议资源
        associateVmr(meetingRoomCreateDTO.getImUserId(), Collections.singletonList(meetingRoomCreateDTO.getVmrId()));

        MeetingClient userMeetingClient = getUserMeetingClient(meetingRoomCreateDTO.getImUserId());

        UpdateMeetingRequest request = new UpdateMeetingRequest();
        request.withConferenceID(meetingRoomCreateDTO.getMeetingCode());
        RestScheduleConfDTO body = new RestScheduleConfDTO();
        RestConfConfigDTO confConfigInfobody = new RestConfConfigDTO();
        confConfigInfobody.withCallInRestriction(2).withAllowGuestStartConf(false)
            .withGuestPwd(meetingRoomCreateDTO.getGuestPwd()).withVmrIDType(1).withProlongLength(30)
            .withEnableWaitingRoom(true);
        body.withVmrID(meetingRoomCreateDTO.getVmrId());
        body.withVmrFlag(1);
        body.withRecordAuthType(1);
        body.withConfConfigInfo(confConfigInfobody);
        body.withRecordType(0);
        body.withTimeZoneID(String.valueOf(meetingRoomCreateDTO.getTimeZoneID()));
        body.withLanguage("zh-CN");
        body.withEncryptMode(2);
        body.withIsAutoRecord(1);
        body.withMediaTypes("HDVideo");
        body.withSubject(meetingRoomCreateDTO.getSubject());
        body.withLength(meetingRoomCreateDTO.getLength());
        body.withStartTime(startTimeStr);
        request.withBody(body);
        UpdateMeetingResponse response = userMeetingClient.updateMeeting(request);
        log.info("编辑云会议会议结果响应：{}", response);
        if (!immediatelyFlag) {
            //为预约会议，预约完成后需要回收资源
            disassociateVmr(meetingRoomCreateDTO.getImUserId(),
                Collections.singletonList(meetingRoomCreateDTO.getVmrId()));
        }
    }

    /**
     * 取消会议
     *
     * @param cancelMeetingRoomModel
     * @return
     */
    @Override
    public void cancelMeetingRoom(CancelMeetingRoomModel cancelMeetingRoomModel) {
        //分配云会议资源
        associateVmr(cancelMeetingRoomModel.getImUserId(),
            Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
        MeetingClient userMeetingClient = getUserMeetingClient(cancelMeetingRoomModel.getImUserId());
        //取消会议
        CancelMeetingRequest request = new CancelMeetingRequest();
        request.withConferenceID(cancelMeetingRoomModel.getConferenceID());
        try {
            CancelMeetingResponse response = userMeetingClient.cancelMeeting(request);
            log.info("取消云会议结果：{}", response);
        } catch (ConnectionException e) {
            log.error("取消云会议连接异常", e);
        } catch (RequestTimeoutException e) {
            log.error("取消云会议连接超时", e);
        } catch (ServiceResponseException e) {
            log.error("取消云会议响应异常", e);
        }
        //回收资源
        disassociateVmr(cancelMeetingRoomModel.getImUserId(),
            Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
    }

    /**
     * 查询尚未结束的会议列表
     *
     * @param imUserId
     * @return
     */
    @Override
    public void queryMeetingRoomList(String imUserId) {

    }

    /**
     * 查询详情
     *
     * @param meetingRoomDetailDTO
     * @return
     */
    @Override
    public void setMeetingRoomDetail(MeetingRoomDetailDTO meetingRoomDetailDTO) {
        ShowMeetingDetailRequest request = new ShowMeetingDetailRequest();
        request.withConferenceID(meetingRoomDetailDTO.getHwMeetingCode());
        ShowMeetingDetailResponse response = meetingClient.showMeetingDetail(request);
        //会议信息
        ConferenceInfo conferenceData = response.getConferenceData();
        List<PasswordEntry> passwordEntry = conferenceData.getPasswordEntry();
        for (PasswordEntry entry : passwordEntry) {
            String conferenceRole = entry.getConferenceRole();
            String password = entry.getPassword();
            if (ObjectUtil.equals("chair", conferenceRole)) {
                //主持人
                meetingRoomDetailDTO.setChairmanPwd(password);
            } else if (ObjectUtil.equals("general", conferenceRole)) {
                //与会者密码
                meetingRoomDetailDTO.setGeneralPwd(password);
            }
        }
//        meetingRoomDetailDTO.setGuestPwd();
//        meetingRoomDetailDTO.setAudiencePasswd();
        meetingRoomDetailDTO.setChairJoinUri(conferenceData.getChairJoinUri());
        meetingRoomDetailDTO.setGuestJoinUri(conferenceData.getGuestJoinUri());
        //网络研讨会观众会议链接地址
        meetingRoomDetailDTO.setAudienceJoinUri(conferenceData.getAudienceJoinUri());

    }

    /**
     * 查询历史会议列表
     *
     * @param imUserId
     * @return
     */
    @Override
    public void queryHistoryMeetingRoomList(String imUserId) {

    }

    /**
     * 查询历史会议详情
     *
     * @param imUserId
     * @param confUUID
     * @return
     */
    @Override
    public void queryHistoryMeetingRoomInfo(String imUserId, String confUUID) {

    }

    /**
     * 查询录制详情
     *
     * @param confUUID
     */
    @Override
    public void queryRecordFiles(String confUUID) {

    }
}
