package com.tiens.meeting.dubboservice.core.impl;

import com.tiens.api.dto.MeetingRoomCreateDTO;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomService;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens 云会议室
 */
@Service("HwMeetingRoomService-2")
@Slf4j
public class SeminarMeetingRoomImpl extends HwMeetingRoomService {
    /**
     * 创建华为会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    @Override
    public MeetingRoomModel createMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO) {
        return null;
    }

    /**
     * 修改华为会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    @Override
    public MeetingRoomModel updateMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO) {
        return null;
    }

    /**
     * 取消会议
     *
     * @param conferenceID
     * @return
     */
    @Override
    public void cancelMeetingRoom(String conferenceID) {

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
     * @param imUserId
     * @param conferenceID
     * @return
     */
    @Override
    public void getMeetingRoomDetail(String imUserId, String conferenceID) {

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
