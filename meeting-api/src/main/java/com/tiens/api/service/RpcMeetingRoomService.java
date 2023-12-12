package com.tiens.api.service;

import com.tiens.api.dto.AvailableResourcePeriodGetDTO;
import com.tiens.api.dto.EnterMeetingRoomCheckDTO;
import com.tiens.api.dto.FreeResourceListDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/9
 * @Version 1.0
 * @Company: tiens
 */
public interface RpcMeetingRoomService {
    /**
     * 获取认证资质
     *
     * @param imUserId
     * @return
     */
    CommonResult<VMMeetingCredentialVO> getCredential(String imUserId);

    /**
     * 加入会议前置校验
     *
     * @param enterMeetingRoomCheckDTO
     * @return
     */
    CommonResult enterMeetingRoomCheck(EnterMeetingRoomCheckDTO enterMeetingRoomCheckDTO);

    /**
     * 获取空闲资源列表
     *
     * @param freeResourceListDTO
     * @return
     */
    CommonResult<List<MeetingResourceVO>> getFreeResourceList(FreeResourceListDTO freeResourceListDTO);

    /**
     * 创建会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    CommonResult createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO);

    /**
     * 编辑会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    CommonResult updateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO);

    /**
     * 查询会议详情
     *
     * @param meetingRoomId
     * @return
     * @oaram imUserId
     */
    CommonResult<MeetingRoomDetailDTO> getMeetingRoom(Long meetingRoomId, String imUserId);

    /**
     * 取消会议
     *
     * @param meetingRoomId
     * @return
     */
    CommonResult cancelMeetingRoom(Long meetingRoomId);

    /**
     * 首页查询即将召开和进行中的会议列表
     *
     * @param imUserId
     * @return
     */
    CommonResult<FutureAndRunningMeetingRoomListVO> getFutureAndRunningMeetingRoomList(String imUserId);

    /**
     * 首页查询历史30天的会议列表
     *
     * @return
     */
    CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList(String imUserId, Integer month);

    /**
     * 查询资源可用的时间段
     *
     * @param availableResourcePeriodGetDTO
     * @return
     */
    CommonResult<List<AvailableResourcePeriodVO>> getAvailableResourcePeriod(
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO);

    /**
     * 更新华为云会议室状态
     *
     * @param hwEventReq
     * @return
     */
    CommonResult<String> updateMeetingRoomStatus(HwEventReq hwEventReq);

    /**
     * 查询会议录制详情
     *
     * @param meetingRoomId
     * @return
     */
    CommonResult<List<RecordVO>> getMeetingRoomRecordList(Long meetingRoomId);

    CommonResult<List<ResourceTypeVO>> getMeetingResourceTypeList();
}
