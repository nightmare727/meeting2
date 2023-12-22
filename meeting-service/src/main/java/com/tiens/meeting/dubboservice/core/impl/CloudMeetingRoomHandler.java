package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.util.date.DateUtils;
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
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public MeetingRoomModel createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        //将开始时间转化成UTC时间

        Date startTime = meetingRoomContextDTO.getStartTime();
        //是否预约会议
        Boolean subsCribeFlag = ObjectUtil.isNotNull(startTime);
        //处理开始时间
        startTime = DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(DateUtil.date(startTime), DateUtil.date()));
        ZoneId zoneId3 = ZoneId.of("GMT");
        DateTime dateTime = DateUtil.convertTimeZone(startTime, zoneId3);
        LocalDateTime of = LocalDateTimeUtil.of(dateTime);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startTimeStr = dateTimeFormatter.format(of);
        try {
            //分配云会议资源
            hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
                Collections.singletonList(meetingRoomContextDTO.getVmrId()));

            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());
            CreateMeetingRequest request = new CreateMeetingRequest();
            RestScheduleConfDTO body = new RestScheduleConfDTO();
            RestConfConfigDTO confConfigInfobody = new RestConfConfigDTO();
            confConfigInfobody.withIsGuestFreePwd(false)
                //允许加入会议的范围。企业
                .withCallInRestriction(2)
                //随机会议id-私人会议，固定会议id
                .withVmrIDType(1)
                //自动延时30分钟
                .withProlongLength(0).withIsGuestFreePwd(meetingRoomContextDTO.getGuestPwdFlag())
                //是否开启等候室
                .withEnableWaitingRoom(false);
            body.withVmrID(meetingRoomContextDTO.getVmrId());
            body.withVmrFlag(1);
            body.withConfConfigInfo(confConfigInfobody);
            //录播类型。默认为禁用。
            //0: 禁用
            //1: 直播
            //2: 录播
            //3: 直播+录播
            body.withRecordType(2);
//            会议是否自动启动录制，在录播类型为：录播、录播+直播时才生效。默认为不自动启动。
//            1：自动启动录制
//            0：不自动启动录制
            body.withIsAutoRecord(0);
            //固定时区GMT+8
            body.withTimeZoneID(String.valueOf(meetingRoomContextDTO.getTimeZoneID()));
            body.withLanguage("zh-CN");
            body.withEncryptMode(2);
            body.withMediaTypes("HDVideo");
            body.withSubject(meetingRoomContextDTO.getSubject());
            body.withLength(meetingRoomContextDTO.getLength());
            //会议开始时间（UTC时间）。格式：yyyy-MM-dd HH:mm。 > * 创建预约会议时，如果没有指定开始时间或填空串，则表示会议马上开始 > * 时间是UTC时间，即0时区的时间
            body.withStartTime(startTimeStr);
            request.withBody(body);
            CreateMeetingResponse response = userMeetingClient.createMeeting(request);
            log.info("创建云会议结果响应：{}", response);
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
        } catch (Exception e) {
            log.error("创建云会议、预约会议异常，异常信息：{}", e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_CREATE_MEETING_ERROR);
        } finally {
            if (subsCribeFlag) {
                //为预约会议，预约完成后需要回收资源
                hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                    Collections.singletonList(meetingRoomContextDTO.getVmrId()));
            }
        }
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
        //是否预约会议
        Boolean subsCribeFlag = ObjectUtil.isNotNull(startTime);
        //处理开始时间
        startTime = DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(DateUtil.date(startTime), DateUtil.date()));
        ZoneId zoneId3 = ZoneId.of("GMT");
        DateTime dateTime = DateUtil.convertTimeZone(startTime, zoneId3);
        LocalDateTime of = LocalDateTimeUtil.of(dateTime);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startTimeStr = dateTimeFormatter.format(of);

        try {
            //分配云会议资源
            hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
                Collections.singletonList(meetingRoomContextDTO.getVmrId()));

            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());

            UpdateMeetingRequest request = new UpdateMeetingRequest();
            request.withConferenceID(meetingRoomContextDTO.getMeetingCode());
            RestScheduleConfDTO body = new RestScheduleConfDTO();
            RestConfConfigDTO confConfigInfobody = new RestConfConfigDTO();
            confConfigInfobody.withCallInRestriction(2).withAllowGuestStartConf(false)
                .withIsGuestFreePwd(meetingRoomContextDTO.getGuestPwdFlag()).withVmrIDType(1).withProlongLength(0)
                .withEnableWaitingRoom(Boolean.FALSE);
            body.withVmrID(meetingRoomContextDTO.getVmrId());
            body.withVmrFlag(1);
            body.withRecordAuthType(1);
            body.withConfConfigInfo(confConfigInfobody);
            body.withRecordType(0);
            body.withTimeZoneID(String.valueOf(meetingRoomContextDTO.getTimeZoneID()));
            body.withLanguage("zh-CN");
            body.withEncryptMode(2);
            //录播类型。默认为禁用。
            //0: 禁用
            //1: 直播
            //2: 录播
            //3: 直播+录播
            body.withRecordType(2);
//            会议是否自动启动录制，在录播类型为：录播、录播+直播时才生效。默认为不自动启动。
//            1：自动启动录制
//            0：不自动启动录制
            body.withIsAutoRecord(0);
            body.withMediaTypes("HDVideo");
            body.withSubject(meetingRoomContextDTO.getSubject());
            body.withLength(meetingRoomContextDTO.getLength());
            body.withStartTime(startTimeStr);
            request.withBody(body);
            UpdateMeetingResponse response = userMeetingClient.updateMeeting(request);
            log.info("编辑云会议会议结果响应：{}", response);
        } catch (Exception e) {
            log.error("编辑云会议、预约会议异常，异常信息：{}", e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_MOD_MEETING_ERROR);
        } finally {
            if (subsCribeFlag) {
                //为预约会议，预约完成后需要回收资源
                hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                    Collections.singletonList(meetingRoomContextDTO.getVmrId()));
            }
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

        try {
            //分配云会议资源
            hwMeetingCommonService.associateVmr(cancelMeetingRoomModel.getImUserId(),
                Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(cancelMeetingRoomModel.getImUserId());
            //取消会议
            CancelMeetingRequest request = new CancelMeetingRequest();
            request.withConferenceID(cancelMeetingRoomModel.getConferenceID());
            CancelMeetingResponse response = userMeetingClient.cancelMeeting(request);
            log.info("取消云会议会议结果响应：{}", response);
        } catch (Exception e) {
            log.error("取消云会议异常，异常信息：{}", e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_CANCEL_MEETING_ERROR);
        } finally {
            //回收资源
            hwMeetingCommonService.disassociateVmr(cancelMeetingRoomModel.getImUserId(),
                Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
        }

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
        try {
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
            meetingRoomDetailDTO.setGuestPwdFlag(conferenceData.getConfConfigInfo().getIsGuestFreePwd());
            meetingRoomDetailDTO.setChairJoinUri(conferenceData.getChairJoinUri());
            meetingRoomDetailDTO.setGuestJoinUri(conferenceData.getGuestJoinUri());
            //网络研讨会观众会议链接地址
            meetingRoomDetailDTO.setAudienceJoinUri(conferenceData.getAudienceJoinUri());
        } catch (Exception e) {
            log.error("取消云会议异常，异常信息：{}", e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_CANCEL_MEETING_ERROR);
        } finally {

        }

    }

    /**
     * 是否存在会议
     *
     * @param meetingCode
     * @return
     */
    @Override
    public Boolean existMeetingRoom(String meetingCode) {
        SearchMeetingsRequest request = new SearchMeetingsRequest();
        request.withQueryAll(true);
        request.withSearchKey(meetingCode);
        SearchMeetingsResponse response = meetingClient.searchMeetings(request);
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
