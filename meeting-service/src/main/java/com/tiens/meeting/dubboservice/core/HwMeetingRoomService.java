package com.tiens.meeting.dubboservice.core;

import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import com.tiens.api.dto.MeetingRoomCreateDTO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 *
 *     定义华为会议相关接口
 */

public abstract class HwMeetingRoomService {

    @Autowired
    MeetingConfig meetingConfig;

    public MeetingClient getUserMeetingClient(String imUserId) {
        MeetingCredentials auth =
            new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(meetingConfig.getAppId())
                .withAppKey(meetingConfig.getAppKey()).withUserId(imUserId);
        MeetingClient client =
            MeetingClient.newBuilder().withCredential(auth).withEndpoints(meetingConfig.getEndpoints()).build();
        return client;
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
    public abstract MeetingRoomModel updateMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO);

    /**
     * 取消会议
     *
     * @param conferenceID
     * @return
     */
    public abstract void cancelMeetingRoom(String conferenceID);

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
     * @param conferenceID
     * @return
     */
    public abstract void getMeetingRoomDetail(String imUserId, String conferenceID);

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
     * @return
     */
    public abstract void queryHistoryMeetingRoomInfo(String imUserId, String confUUID);

    /**
     * 查询录制详情
     *
     * @param confUUID
     */
    public abstract void queryRecordFiles(String confUUID);

}
