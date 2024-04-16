package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
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
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 创建华为会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public MeetingRoomModel createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        //资源是否公有
        boolean publicFlag = NumberUtil.isNumber(meetingRoomContextDTO.getResourceType());
        Date startTime = meetingRoomContextDTO.getStartTime();
        //是否预约会议
        Boolean subsCribeFlag = ObjectUtil.isNotNull(startTime);

        //当前UTC时间
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);

        //处理开始时间
        startTime = DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(startTime, now), DateUtils.TIME_ZONE_GMT);

        //锁定开始时间
        DateTime lockStartTime = DateUtil.offsetMinute(startTime, -30);
        subsCribeFlag = subsCribeFlag && now.isBefore(lockStartTime);

        String startTimeStr = DateUtil.format(startTime, dateTimeFormatter);
        Boolean exceptionHappenFlag = false;
        try {
            //分配云会议资源
            if (publicFlag) {
                hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
                    Collections.singletonList(meetingRoomContextDTO.getVmrId()));
            }

            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());
            CreateMeetingRequest request = new CreateMeetingRequest();
            RestScheduleConfDTO body = new RestScheduleConfDTO();
            RestConfConfigDTO confConfigInfobody = new RestConfConfigDTO();
            //是否私人会议
            boolean isPrivate = !NumberUtil.isNumber(meetingRoomContextDTO.getResourceType());
            confConfigInfobody.withIsGuestFreePwd(false)
                //允许加入会议的范围。企业
                .withCallInRestriction(2)
                //随机会议id-私人会议，固定会议id
                .withVmrIDType(isPrivate ? 0 : 1)
                //私人会议延长60分钟，否则不延迟
                .withProlongLength(isPrivate ? 60 : 0).withIsGuestFreePwd(meetingRoomContextDTO.getGuestPwdFlag())
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
            body.withLength(meetingRoomContextDTO.getLength() + 60);
            //会议开始时间（UTC时间）。格式：yyyy-MM-dd HH:mm。 > * 创建预约会议时，如果没有指定开始时间或填空串，则表示会议马上开始 > * 时间是UTC时间，即0时区的时间
            body.withStartTime(startTimeStr);
            body.withSupportSimultaneousInterpretation(Boolean.TRUE);

            request.withBody(body);
            log.info("创建云会议结果入参：{}", JSON.toJSONString(request));
            CreateMeetingResponse response = userMeetingClient.createMeeting(request);
            log.info("创建云会议结果响应：{}", JSON.toJSONString(response));
            List<ConferenceInfo> body1 = response.getBody();
            ConferenceInfo conferenceInfo = body1.get(0);
            //会议id
            String conferenceID = conferenceInfo.getConferenceID();
            //云会议室会议ID或个人会议ID。如果“vmrFlag”为“1”，则该字段不为空。
            String vmrID = conferenceInfo.getVmrID();
            //会议的UUID-只有创建立即开始的会议才返回UUID，如果是预约未来的会议，不会返回UUID
            String confUUID = conferenceInfo.getConfUUID();
            String conferenceState = conferenceInfo.getConferenceState();
            //主持人密码
            String chairPwd = "";
            String generalPwd = "";
            for (PasswordEntry passwordEntry : conferenceInfo.getPasswordEntry()) {
                if (passwordEntry.getConferenceRole().equals("chair")) {
                    chairPwd = passwordEntry.getPassword();
                } else if (passwordEntry.getConferenceRole().equals("general")) {
                    generalPwd = passwordEntry.getPassword();
                }
            }
            MeetingRoomModel meetingRoomModel = new MeetingRoomModel();
            meetingRoomModel.setHwMeetingId(confUUID);
            meetingRoomModel.setHwMeetingCode(conferenceID);
            meetingRoomModel.setState(conferenceState);
            meetingRoomModel.setChairmanPwd(chairPwd);
            meetingRoomModel.setGeneralPwd(generalPwd);

            return meetingRoomModel;
        } catch (Exception e) {
            log.error("创建云会议、预约会议异常，异常信息：{}", e);
            exceptionHappenFlag = Boolean.TRUE;
            throw new ServiceException(GlobalErrorCodeConstants.HW_CREATE_MEETING_ERROR);
        } finally {
            if (publicFlag) {
                if (subsCribeFlag) {
                    //为预约会议，预约完成后需要回收资源
                    log.info("编辑资源回收达成条件是 新增 publicFlag:{},subsCribeFlag:{}", publicFlag, subsCribeFlag);
                    String currentResourceUserId = meetingRoomContextDTO.getCurrentResourceUserId();
                    if (ObjectUtil.isNotEmpty(currentResourceUserId)) {
                        hwMeetingCommonService.associateVmr(currentResourceUserId,
                            Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                    } else {
                        //如果已分配，则执行 回收-分配-再回收
                        hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                            Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                    }
                } else if (exceptionHappenFlag) {
                    //立即会议发生异常
                    hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                        Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                }

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
        boolean publicFlag = NumberUtil.isNumber(meetingRoomContextDTO.getResourceType());

        Date startTime = meetingRoomContextDTO.getStartTime();
        //是否预约会议
        Boolean subsCribeFlag = ObjectUtil.isNotNull(startTime);
        //当前UTC时间
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));

        //处理开始时间
        startTime = DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(startTime, now), DateUtils.TIME_ZONE_GMT);

        //锁定开始时间
        DateTime lockStartTime = DateUtil.offsetMinute(startTime, -30);
        subsCribeFlag = subsCribeFlag && now.isBefore(lockStartTime);

        String startTimeStr = DateUtil.format(startTime, dateTimeFormatter);

        try {
            //分配云会议资源
            if (publicFlag) {
                hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
                    Collections.singletonList(meetingRoomContextDTO.getVmrId()));
            }

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
            body.withLength(meetingRoomContextDTO.getLength() + 60);
            body.withStartTime(startTimeStr);
            request.withBody(body);
            log.info("编辑云会议会议结果入参：{}", request);
            UpdateMeetingResponse response = userMeetingClient.updateMeeting(request);
            log.info("编辑云会议会议结果响应：{}", response);
        } catch (Exception e) {
            log.error("编辑云会议、预约会议异常，异常信息：{}", e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_MOD_MEETING_ERROR);
        } finally {
            if (publicFlag && subsCribeFlag) {
                //为预约会议，预约完成后需要回收资源
                log.info("编辑资源回收达成条件是 publicFlag:{},subsCribeFlag:{}", publicFlag, subsCribeFlag);
                String currentResourceUserId = meetingRoomContextDTO.getCurrentResourceUserId();
                if (ObjectUtil.isNotEmpty(currentResourceUserId)) {
                    hwMeetingCommonService.associateVmr(currentResourceUserId,
                        Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                } else {
                    //如果已分配，则执行 回收-分配-再回收
                    hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                        Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                }
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
        Boolean publicFlag = cancelMeetingRoomModel.getPublicFlag();
        try {
            if (publicFlag) {
                //分配云会议资源
                hwMeetingCommonService.associateVmr(cancelMeetingRoomModel.getImUserId(),
                    Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
            }
            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(cancelMeetingRoomModel.getImUserId());
            //取消会议
            CancelMeetingRequest request = new CancelMeetingRequest();
            request.withConferenceID(cancelMeetingRoomModel.getConferenceID());
            log.info("取消云会议会议结果入参：{}", request);
            CancelMeetingResponse response = userMeetingClient.cancelMeeting(request);
            log.info("取消云会议会议结果响应：{}", response);
        } catch (ServiceResponseException e) {
            if (e.getErrorCode().equals("MMC.111070005")) {
                log.info("取消华为云--云会议信息不存在，当前会议号:{}", cancelMeetingRoomModel.getConferenceID());
            } else {
                log.error("取消华为云--云会议信息发生其他异常，当前会议号:{}，异常信息：{}",
                    cancelMeetingRoomModel.getConferenceID(), e);
            }
        } catch (Exception e) {
            log.error("取消华为云--云会议信息发生系统异常，当前会议号:{}，异常信息：{}",
                cancelMeetingRoomModel.getConferenceID(), e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_CANCEL_MEETING_ERROR);
        } finally {
            //回收资源
            if (publicFlag) {
                String currentResourceUserId = cancelMeetingRoomModel.getCurrentResourceUserId();
                if (ObjectUtil.isNotEmpty(currentResourceUserId)) {
                    hwMeetingCommonService.associateVmr(currentResourceUserId,
                        Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
                } else {
                    //如果已分配，则执行 回收-分配-再回收
                    hwMeetingCommonService.disassociateVmr(cancelMeetingRoomModel.getImUserId(),
                        Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
                }
            }
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
            MeetingClient meetingClient = hwMeetingCommonService.getMgrMeetingClient();
            log.info("查询华为云会议详情入参：{}", request);
            ShowMeetingDetailResponse response = meetingClient.showMeetingDetail(request);
            log.info("查询华为云会议详情结果：{}", response);
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
        } catch (ServiceResponseException e) {
            if (e.getErrorCode().equals("MMC.111070005")) {
                log.error("查询华为云会议信息不存在，当前会议号:{}，异常信息：{}", meetingRoomDetailDTO.getHwMeetingCode(),
                    e);
            } else {
                log.error("查询华为云会议信息发生其他异常，当前会议号:{}，异常信息：{}",
                    meetingRoomDetailDTO.getHwMeetingCode(), e);
            }
        } catch (Exception e) {
            log.error("查询华为云会议信息系统异常，当前会议号:{}，异常信息：{}", meetingRoomDetailDTO.getHwMeetingCode(),
                e);
//            throw new ServiceException(GlobalErrorCodeConstants.HW_CANCEL_MEETING_ERROR);
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
        log.info("查询是否存在会议入参：{}", request);
        MeetingClient meetingClient = hwMeetingCommonService.getMgrMeetingClient();
        SearchMeetingsResponse response = meetingClient.searchMeetings(request);
        log.info("查询是否存在会议返回：{}", response);
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
