package com.tiens.api.service;

import com.tiens.api.dto.*;
import com.tiens.api.vo.AvailableResourcePeriodVO;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.VMMeetingCredentialVO;
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
     * @param meetingRoomCreateDTO
     * @return
     */
    CommonResult createMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO);

    /**
     * 编辑会议
     *
     * @param meetingRoomUpdateDTO
     * @return
     */
    CommonResult updateMeetingRoom(MeetingRoomUpdateDTO meetingRoomUpdateDTO);

    /**
     * 查询会议详情
     *
     * @param meetingRoomId
     * @return
     */
    CommonResult<MeetingRoomDetailDTO> getMeetingRoom(Long meetingRoomId);

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
     * @return
     */
    CommonResult<List<MeetingRoomDetailDTO>> getFutureAndRunningMeetingRoomList();

    /**
     * 首页查询历史30天的会议列表
     *
     * @return
     */
    CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList();

    /**
     * 查询资源可用的时间段
     *
     * @param availableResourcePeriodGetDTO
     * @return
     */
    CommonResult<List<AvailableResourcePeriodVO>> getAvailableResourcePeriod(
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO);

}
