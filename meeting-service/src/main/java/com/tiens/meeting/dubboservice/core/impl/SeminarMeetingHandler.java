package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import common.enums.MeetingRoomStateEnum;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.util.date.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 创建华为会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public MeetingRoomModel createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        // 资源是否公有
        boolean publicFlag = NumberUtil.isNumber(meetingRoomContextDTO.getResourceType());

        Date startTime = meetingRoomContextDTO.getStartTime();

        Integer leadTime = meetingRoomContextDTO.getLeadTime();

        // 是否预约会议
        Boolean subsCribeFlag = ObjectUtil.isNotNull(startTime);

        // 当前UTC时间
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);
        // 处理开始时间
        startTime = DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(startTime, now), DateUtils.TIME_ZONE_GMT);

        // 锁定开始时间
        DateTime lockStartTime = DateUtil.offsetMinute(startTime, -leadTime);
        subsCribeFlag = subsCribeFlag && now.isBefore(lockStartTime);
        String startTimeStr = DateUtil.format(startTime, dateTimeFormatter);
        Boolean exceptionHappenFlag = false;
        try {
            if (publicFlag) {
                // 分配云会议资源
                hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
                    Collections.singletonList(meetingRoomContextDTO.getVmrId()));
            }

            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());

            CreateWebinarRequest request = new CreateWebinarRequest();
            OpenScheduleConfReq body = new OpenScheduleConfReq();
            // 开启录制
            body.withEnableRecording(YesNoEnum.N);
            // 设置入会范围开关
            body.withCallRestriction(true);
            // 观众入会范围
            body.withAudienceScope(2);
            // 主持人、嘉宾入会范围
            body.withScope(2);
            // 嘉宾密码
            // body.withGuestPasswd(RandomUtil.randomNumbers(6));
            body.withTimeZoneId(meetingRoomContextDTO.getTimeZoneID());
            body.withDuration(meetingRoomContextDTO.getLength() + 60);
            body.withStartTime(startTimeStr);
            body.withSubject(meetingRoomContextDTO.getSubject());
            body.withVmrID(meetingRoomContextDTO.getVmrId());
            request.withBody(body);
            log.info("创建网络研讨会会议结果入参：{}", request);
            CreateWebinarResponse response = userMeetingClient.createWebinar(request);
            log.info("创建网络研讨会会议结果响应：{}", response);

            // 会议id
            String conferenceId = response.getConferenceId();
            String state = response.getState().getValue();
            MeetingRoomModel meetingRoomModel = new MeetingRoomModel();
            meetingRoomModel.setHwMeetingId("");
            meetingRoomModel.setHwMeetingCode(conferenceId);
            meetingRoomModel.setState(state);
            meetingRoomModel.setChairmanPwd(response.getChairPasswd());
            meetingRoomModel.setGuestPwd(response.getGuestPasswd());
            meetingRoomModel.setAudiencePasswd(response.getAudiencePasswd());

            return meetingRoomModel;
        } catch (Exception e) {
            log.error("创建网络研讨会会议、预约会议异常，异常信息：{}", e);
            exceptionHappenFlag = Boolean.TRUE;
            throw new ServiceException(GlobalErrorCodeConstants.HW_CREATE_MEETING_ERROR);
        } finally {
            if (publicFlag) {
                if (subsCribeFlag) {
                    // 为预约会议，预约完成后需要回收资源
                    log.info("编辑资源回收达成条件是 新增 publicFlag:{},subsCribeFlag:{}", publicFlag, subsCribeFlag);
                    String currentResourceUserId = meetingRoomContextDTO.getCurrentResourceUserId();
                    if (ObjectUtil.isNotEmpty(currentResourceUserId)) {
                        hwMeetingCommonService.associateVmr(currentResourceUserId,
                            Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                    } else {
                        // 如果已分配，则执行 回收-分配-再回收
                        hwMeetingCommonService.disassociateVmr(meetingRoomContextDTO.getImUserId(),
                            Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                    }
                } else if (exceptionHappenFlag) {
                    // 立即会议发生异常
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

        Integer leadTime = meetingRoomContextDTO.getLeadTime();

        // 是否预约会议
        Boolean subsCribeFlag = ObjectUtil.isNotNull(startTime);
        // 当前UTC时间
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);
        // 处理开始时间
        startTime = DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(startTime, now), DateUtils.TIME_ZONE_GMT);

        // 锁定开始时间
        DateTime lockStartTime = DateUtil.offsetMinute(startTime, -leadTime);
        subsCribeFlag = subsCribeFlag && now.isBefore(lockStartTime);
        String startTimeStr = DateUtil.format(startTime, dateTimeFormatter);
        try {
            // 分配云会议资源
            if (publicFlag) {
                hwMeetingCommonService.associateVmr(meetingRoomContextDTO.getImUserId(),
                    Collections.singletonList(meetingRoomContextDTO.getVmrId()));
            }

            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(meetingRoomContextDTO.getImUserId());

            // 编辑网络研讨会会议
            UpdateWebinarRequest request = new UpdateWebinarRequest();
            OpenEditConfReq body = new OpenEditConfReq();
            body.withConferenceId(meetingRoomContextDTO.getMeetingCode());
            body.withSubject(meetingRoomContextDTO.getSubject());
            body.withEnableRecording(YesNoEnum.N);
            body.withAudienceScope(2);
            body.withScope(2);
            body.withCallRestriction(true);
            // body.withGuestPasswd(RandomUtil.randomNumbers(6));
            body.withTimeZoneId(meetingRoomContextDTO.getTimeZoneID());
            body.withDuration(meetingRoomContextDTO.getLength() + 60);
            body.withStartTime(startTimeStr);
            body.withConferenceId(meetingRoomContextDTO.getMeetingCode());
            request.withBody(body);
            log.info("编辑网络研讨会会议结果入参：{}", request);
            UpdateWebinarResponse response = userMeetingClient.updateWebinar(request);
            log.info("编辑网络研讨会会议结果响应：{}", response);

        } catch (Exception e) {
            log.error("编辑网络研讨会会议异常，异常信息：{}", e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_MOD_MEETING_ERROR);
        } finally {
            if (publicFlag & subsCribeFlag) {
                // 为预约会议，预约完成后需要回收资源
                String currentResourceUserId = meetingRoomContextDTO.getCurrentResourceUserId();
                if (ObjectUtil.isNotEmpty(currentResourceUserId)) {
                    hwMeetingCommonService.associateVmr(currentResourceUserId,
                        Collections.singletonList(meetingRoomContextDTO.getVmrId()));
                } else {
                    // 如果已分配，则执行 回收-分配-再回收
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
                // 分配云会议资源
                hwMeetingCommonService.associateVmr(cancelMeetingRoomModel.getImUserId(),
                    Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
            }
            MeetingClient userMeetingClient =
                hwMeetingCommonService.getUserMeetingClient(cancelMeetingRoomModel.getImUserId());
            // 取消会议
            DeleteWebinarRequest request = new DeleteWebinarRequest();
            request.withConferenceId(cancelMeetingRoomModel.getConferenceID());
            log.info("取消网络研讨会入参：{}", request);
            DeleteWebinarResponse response = userMeetingClient.deleteWebinar(request);
            log.info("取消网络研讨会结果：{}", response);

        } catch (ServiceResponseException e) {
            if (e.getErrorCode().equals("MMC.111070005")) {
                log.info("取消华为云--网络研讨会信息不存在，当前会议号:{}", cancelMeetingRoomModel.getConferenceID());
            } else {
                log.error("取消华为云--网络研讨会信息发生其他异常，当前会议号:{}，异常信息：{}",
                    cancelMeetingRoomModel.getConferenceID(), e);
            }
        } catch (Exception e) {
            log.error("取消华为云--网络研讨会发生系统异常，当前会议号:{}，异常信息：{}",
                cancelMeetingRoomModel.getConferenceID(), e);
            throw new ServiceException(GlobalErrorCodeConstants.HW_CANCEL_MEETING_ERROR);
        } finally {
            // 回收资源
            if (publicFlag) {
                String currentResourceUserId = cancelMeetingRoomModel.getCurrentResourceUserId();
                if (ObjectUtil.isNotEmpty(currentResourceUserId)) {
                    hwMeetingCommonService.associateVmr(currentResourceUserId,
                        Collections.singletonList(cancelMeetingRoomModel.getVmrId()));
                } else {
                    // 如果已分配，则执行 回收-分配-再回收
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
        String state = meetingRoomDetailDTO.getState();
        MeetingClient meetingClient = hwMeetingCommonService.getMgrMeetingClient();

        if (MeetingRoomStateEnum.Schedule.getState().equals(state)) {
            ListUpComingWebinarsRequest request = new ListUpComingWebinarsRequest();
            request.withSearchKey(meetingRoomDetailDTO.getConferenceId());
            log.info("即将招开的研讨会会议查询入参：{}", request);
            ListUpComingWebinarsResponse response = meetingClient.listUpComingWebinars(request);
            log.info("即将招开的研讨会会议查询结果：{}", response);
            List<OpenWebinarUpcomingInfo> data = response.getData();
            if (CollectionUtil.isEmpty(data)) {
                log.info("即将招开的研讨会会议查询不到数据，会议号：{}", meetingRoomDetailDTO.getHwMeetingCode());
                return;
            }
            OpenWebinarUpcomingInfo openWebinarUpcomingInfo = data.get(0);

            meetingRoomDetailDTO.setChairmanPwd(openWebinarUpcomingInfo.getChairPasswd());
            // meetingRoomDetailDTO.setGeneralPwd(response);
            meetingRoomDetailDTO.setGuestPwd(openWebinarUpcomingInfo.getGuestPasswd());
            meetingRoomDetailDTO.setAudiencePasswd(openWebinarUpcomingInfo.getAudiencePasswd());
            meetingRoomDetailDTO.setChairJoinUri(openWebinarUpcomingInfo.getChairJoinUri());
            meetingRoomDetailDTO.setGuestJoinUri(openWebinarUpcomingInfo.getGuestJoinUri());
            // 网络研讨会观众会议链接地址
            meetingRoomDetailDTO.setAudienceJoinUri(openWebinarUpcomingInfo.getAudienceJoinUri());
        } else if (MeetingRoomStateEnum.Created.getState().equals(state)) {
            ListOngoingWebinarsRequest listOngoingWebinarsRequest = new ListOngoingWebinarsRequest();
            listOngoingWebinarsRequest.withSearchKey(meetingRoomDetailDTO.getConferenceId());
            log.info("正在进行的研讨会会议查询入参：{}", listOngoingWebinarsRequest);
            ListOngoingWebinarsResponse listOngoingWebinarsResponse =
                meetingClient.listOngoingWebinars(listOngoingWebinarsRequest);
            log.info("正在进行的研讨会会议查询结果：{}", listOngoingWebinarsResponse);
            List<OpenWebinarOngoingInfo> data = listOngoingWebinarsResponse.getData();
            if (CollectionUtil.isEmpty(data)) {
                log.info("进行中的研讨会会议查询不到数据，会议号：{}", meetingRoomDetailDTO.getHwMeetingCode());
                return;
            }
            OpenWebinarOngoingInfo openWebinarOngoingInfo = data.get(0);
            meetingRoomDetailDTO.setChairmanPwd(openWebinarOngoingInfo.getChairPasswd());
            // meetingRoomDetailDTO.setGeneralPwd(response);
            meetingRoomDetailDTO.setGuestPwd(openWebinarOngoingInfo.getGuestPasswd());
            meetingRoomDetailDTO.setAudiencePasswd(openWebinarOngoingInfo.getAudiencePasswd());
            meetingRoomDetailDTO.setChairJoinUri(openWebinarOngoingInfo.getChairJoinUri());
            meetingRoomDetailDTO.setGuestJoinUri(openWebinarOngoingInfo.getGuestJoinUri());
            // 网络研讨会观众会议链接地址
            meetingRoomDetailDTO.setAudienceJoinUri(openWebinarOngoingInfo.getAudienceJoinUri());
        } else if (MeetingRoomStateEnum.Destroyed.getState().equals(state)) {
            ListHistoryWebinarsRequest listHistoryWebinarsRequest = new ListHistoryWebinarsRequest();
            listHistoryWebinarsRequest.withSearchKey(meetingRoomDetailDTO.getConferenceId());
            log.info("查询历史研讨会会议查询入参：{}", listHistoryWebinarsRequest);
            ListHistoryWebinarsResponse listHistoryWebinarsResponse =
                meetingClient.listHistoryWebinars(listHistoryWebinarsRequest);
            log.info("查询历史研讨会会议查询结果：{}", listHistoryWebinarsResponse);
            List<OpenWebinarHistoryInfo> data = listHistoryWebinarsResponse.getData();
            if (CollectionUtil.isEmpty(data)) {
                log.info("历史研讨会会议查询不到数据，会议号：{}", meetingRoomDetailDTO.getHwMeetingCode());
                return;
            }
            OpenWebinarHistoryInfo openWebinarHistoryInfo = data.get(0);
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
        MeetingClient meetingClient = hwMeetingCommonService.getMgrMeetingClient();

        // 判断是否又即将召开的会议
        ListUpComingWebinarsRequest request = new ListUpComingWebinarsRequest();
        request.withSearchKey(meetingCode);
        log.info("查询是否有即将召开的网络研讨会入参：{}", request);
        ListUpComingWebinarsResponse response = meetingClient.listUpComingWebinars(request);
        log.info("查询是否有即将召开的网络研讨会结果：{}", response);

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
