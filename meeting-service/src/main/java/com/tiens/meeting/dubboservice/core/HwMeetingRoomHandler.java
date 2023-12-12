package com.tiens.meeting.dubboservice.core;

import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    public HwMeetingCommonService hwMeetingCommonService;

    /**
     * 创建华为会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    public abstract MeetingRoomModel createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO);

    /**
     * 修改华为会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    public abstract void updateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO);

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
     * 是否存在会议
     *
     * @param meetingCode
     * @return
     */
    public abstract Boolean existMeetingRoom(String meetingCode);

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

}
