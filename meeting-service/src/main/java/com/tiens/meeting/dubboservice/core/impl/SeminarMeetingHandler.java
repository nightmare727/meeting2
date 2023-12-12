package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.RecordVO;
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
 * @Company: tiens 研讨会
 */
@Service
@Slf4j
public class SeminarMeetingHandler extends HwMeetingRoomHandler {
    /**
     * 创建华为会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public MeetingRoomModel createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        //将开始时间转化成UTC时间

        Date startTime = meetingRoomContextDTO.getStartTime();
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
        hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
            Collections.singletonList(meetingRoomContextDTO.getVmrId()));

        MeetingClient userMeetingClient =  hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());

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
        //嘉宾密码
//        body.withGuestPasswd(RandomUtil.randomNumbers(6));
        body.withTimeZoneId(meetingRoomContextDTO.getTimeZoneID());
        body.withDuration(meetingRoomContextDTO.getLength());
        body.withStartTime(startTimeStr);
        body.withSubject(meetingRoomContextDTO.getSubject());
        body.withVmrID(meetingRoomContextDTO.getVmrId());
        request.withBody(body);
        CreateWebinarResponse response = userMeetingClient.createWebinar(request);
        log.info("创建网络研讨会会议结果响应：{}", response);
        if (!immediatelyFlag) {
            //为预约会议，预约完成后需要回收资源
            hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                Collections.singletonList(meetingRoomContextDTO.getVmrId()));
        }
        //会议id
        String conferenceId = response.getConferenceId();
        String state = response.getState().getValue();
        return new MeetingRoomModel("", conferenceId, state);

    }

    /**
     * 修改华为会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public void updateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        Date startTime = meetingRoomContextDTO.getStartTime();
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
        hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(), Collections.singletonList(meetingRoomContextDTO.getVmrId()));

        MeetingClient userMeetingClient =  hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());

        //编辑网络研讨会会议
        UpdateWebinarRequest request = new UpdateWebinarRequest();
        OpenEditConfReq body = new OpenEditConfReq();
        body.withConferenceId(meetingRoomContextDTO.getMeetingCode());
        body.withSubject(meetingRoomContextDTO.getSubject());
        body.withEnableRecording(YesNoEnum.Y);
        body.withAudienceScope(2);
        body.withScope(2);
        body.withCallRestriction(true);
//        body.withGuestPasswd(RandomUtil.randomNumbers(6));
        body.withTimeZoneId(meetingRoomContextDTO.getTimeZoneID());
        body.withDuration(meetingRoomContextDTO.getLength());
        body.withStartTime(startTimeStr);
        body.withConferenceId(meetingRoomContextDTO.getMeetingCode());
        request.withBody(body);
        UpdateWebinarResponse response = userMeetingClient.updateWebinar(request);
        log.info("编辑网络研讨会会议结果响应：{}", response);
        if (!immediatelyFlag) {
            //为预约会议，预约完成后需要回收资源
            hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                Collections.singletonList(meetingRoomContextDTO.getVmrId()));
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
        hwMeetingCommonService.associateVmr(cancelMeetingRoomModel.getImUserId(),
            Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
        MeetingClient userMeetingClient =  hwMeetingCommonService.getUserMeetingClient(cancelMeetingRoomModel.getImUserId());
        //取消会议
        DeleteWebinarRequest request = new DeleteWebinarRequest();
        request.withConferenceId(cancelMeetingRoomModel.getConferenceID());
        DeleteWebinarResponse response = userMeetingClient.deleteWebinar(request);
        log.info("取消网络研讨会结果：{}", response);
        //回收资源
        hwMeetingCommonService.disassociateVmr(cancelMeetingRoomModel.getImUserId(),
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

        ListUpComingWebinarsRequest request = new ListUpComingWebinarsRequest();
        request.withSearchKey(meetingRoomDetailDTO.getHwMeetingCode());
        ListUpComingWebinarsResponse response = meetingClient.listUpComingWebinars(request);
        OpenWebinarUpcomingInfo openWebinarUpcomingInfo = response.getData().get(0);
        meetingRoomDetailDTO.setChairmanPwd(openWebinarUpcomingInfo.getChairPasswd());
//        meetingRoomDetailDTO.setGeneralPwd(response);
        meetingRoomDetailDTO.setGuestPwd(openWebinarUpcomingInfo.getGuestPasswd());
        meetingRoomDetailDTO.setAudiencePasswd(openWebinarUpcomingInfo.getAudiencePasswd());
        meetingRoomDetailDTO.setChairJoinUri(openWebinarUpcomingInfo.getChairJoinUri());
        meetingRoomDetailDTO.setGuestJoinUri(openWebinarUpcomingInfo.getGuestJoinUri());
        //网络研讨会观众会议链接地址
        meetingRoomDetailDTO.setAudienceJoinUri(openWebinarUpcomingInfo.getAudienceJoinUri());
    }

    /**
     * 是否存在会议
     *
     * @param meetingCode
     * @return
     */
    @Override
    public Boolean existMeetingRoom(String meetingCode) {

        //判断是否又即将召开的会议
        ListUpComingWebinarsRequest request = new ListUpComingWebinarsRequest();
        request.withSearchKey(meetingCode);
        ListUpComingWebinarsResponse response = meetingClient.listUpComingWebinars(request);
        return response.getCount() > 0;
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

}
