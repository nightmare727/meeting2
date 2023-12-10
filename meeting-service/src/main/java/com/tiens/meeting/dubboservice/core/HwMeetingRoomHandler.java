package com.tiens.meeting.dubboservice.core;

import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingRoomCreateDTO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 *
 *     定义华为会议相关接口
 */
@Slf4j
public abstract class HwMeetingRoomHandler {

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    public MeetingClient meetingClient;

    public MeetingClient getUserMeetingClient(String imUserId) {
        MeetingCredentials auth =
            new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(meetingConfig.getAppId())
                .withAppKey(meetingConfig.getAppKey()).withUserId(imUserId);
        MeetingClient client =
            MeetingClient.newBuilder().withCredential(auth).withEndpoints(meetingConfig.getEndpoints()).build();
        return client;
    }

    /**
     * 分配云会议室
     *
     * @param imUserId
     * @param vmrIds
     */
    public void associateVmr(String imUserId, List<String> vmrIds) {
        AssociateVmrRequest request = new AssociateVmrRequest();
        request.withAccount(imUserId);
        request.withBody(vmrIds);
        request.setAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        AssociateVmrResponse response = meetingClient.associateVmr(request);
        log.info("分配云会议室结果：{}", response);
    }

    /**
     * 回收云会议室
     *
     * @param imUserId
     * @param vmrIds
     */
    public void disassociateVmr(String imUserId, List<String> vmrIds) {
        DisassociateVmrRequest request = new DisassociateVmrRequest();
        request.withAccount(imUserId);
        request.withBody(vmrIds);
        request.setAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        try {
            DisassociateVmrResponse response = meetingClient.disassociateVmr(request);
            log.info("回收云会议室结果：{}", response);
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建华为会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    public abstract MeetingRoomModel createMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO);

    /**
     * 修改华为会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    public abstract void updateMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO);

    /**
     * 取消会议
     *
     * @param cancelMeetingRoomModel
     * @return
     */
    public abstract void cancelMeetingRoom(CancelMeetingRoomModel cancelMeetingRoomModel);

    /**
     * 查询尚未结束的会议列表
     *
     * @param imUserId
     * @return
     */
    public abstract void queryMeetingRoomList(String imUserId);

    /**
     * 查询详情
     *
     * @param meetingRoomDetailDTO
     * @return
     */
    public abstract void setMeetingRoomDetail(MeetingRoomDetailDTO meetingRoomDetailDTO);

    /**
     * 查询历史会议列表
     *
     * @param imUserId
     * @return
     */
    public abstract void queryHistoryMeetingRoomList(String imUserId);

    /**
     * 查询历史会议详情
     *
     * @param imUserId
     * @param confUUID
     * @return
     */
    public abstract void queryHistoryMeetingRoomInfo(String imUserId, String confUUID);

    /**
     * 查询录制详情 11
     *
     * @param confUUID
     */
    public abstract void queryRecordFiles(String confUUID);

}
