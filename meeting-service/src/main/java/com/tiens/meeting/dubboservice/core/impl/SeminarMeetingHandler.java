package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
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

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens 研讨会
 */
@Service
@Slf4j
public class SeminarMeetingHandler extends HwMeetingRoomHandler {
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

        CreateWebinarRequest request = new CreateWebinarRequest();
        OpenScheduleConfReq body = new OpenScheduleConfReq();
        //开启录制
        body.withEnableRecording(YesNoEnum.Y);
        //设置入会范围开关
        body.withCallRestriction(true);
        //观众入会范围
        body.withAudienceScope(2);
        //主持人、嘉宾入会范围
        body.withScope(2);
        //观众密码
        body.withAudiencePasswd(meetingRoomCreateDTO.getGuestPwd());
        //嘉宾密码
        body.withGuestPasswd(meetingRoomCreateDTO.getGuestPwd());
        body.withTimeZoneId(meetingRoomCreateDTO.getTimeZoneID());
        body.withDuration(meetingRoomCreateDTO.getLength());
        body.withStartTime(startTimeStr);
        body.withSubject(meetingRoomCreateDTO.getSubject());
        body.withVmrID(meetingRoomCreateDTO.getVmrId());
        request.withBody(body);
        CreateWebinarResponse response = userMeetingClient.createWebinar(request);
        log.info("创建网络研讨会会议结果响应：{}", response);
        if (!immediatelyFlag) {
            //为预约会议，预约完成后需要回收资源
            disassociateVmr(meetingRoomCreateDTO.getImUserId(),
                Collections.singletonList(meetingRoomCreateDTO.getVmrId()));
        }
        //会议id
        String conferenceId = response.getConferenceId();
        String state = response.getState().getValue();
        return new MeetingRoomModel("", conferenceId, state);

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

        UpdateWebinarRequest request = new UpdateWebinarRequest();
        OpenEditConfReq body = new OpenEditConfReq();
        body.withEnableRecording(YesNoEnum.Y);
        body.withAudienceScope(2);
        body.withScope(2);
        body.withCallRestriction(true);
        body.withAudiencePasswd(meetingRoomCreateDTO.getGuestPwd());
        body.withGuestPasswd(meetingRoomCreateDTO.getGuestPwd());
        body.withTimeZoneId(meetingRoomCreateDTO.getTimeZoneID());
        body.withDuration(meetingRoomCreateDTO.getLength());
        body.withStartTime(startTimeStr);
        body.withConferenceId(meetingRoomCreateDTO.getMeetingCode());
        request.withBody(body);
        UpdateWebinarResponse response = userMeetingClient.updateWebinar(request);
        log.info("编辑网络研讨会会议结果响应：{}", response);
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
        DeleteWebinarRequest request = new DeleteWebinarRequest();
        request.withConferenceId(cancelMeetingRoomModel.getConferenceID());
        DeleteWebinarResponse response = userMeetingClient.deleteWebinar(request);
        log.info("取消网络研讨会结果：{}", response);
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
        ShowWebinarRequest request = new ShowWebinarRequest();
        request.withConferenceId(meetingRoomDetailDTO.getHwMeetingCode());
        ShowWebinarResponse response = meetingClient.showWebinar(request);
        meetingRoomDetailDTO.setChairmanPwd(response.getChairPasswd());
//        meetingRoomDetailDTO.setGeneralPwd(response);
        meetingRoomDetailDTO.setGuestPwd(response.getGuestPasswd());
        meetingRoomDetailDTO.setAudiencePasswd(response.getAudiencePasswd());
        meetingRoomDetailDTO.setChairJoinUri(response.getChairJoinUri());
        meetingRoomDetailDTO.setGuestJoinUri(response.getGuestJoinUri());
        //网络研讨会观众会议链接地址
        meetingRoomDetailDTO.setAudienceJoinUri(response.getAudienceJoinUri());
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
