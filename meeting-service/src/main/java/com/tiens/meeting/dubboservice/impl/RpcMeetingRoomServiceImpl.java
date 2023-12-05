package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.util.RandomUtil;
import com.huaweicloud.sdk.meeting.v1.utils.HmacSHA256;
import com.tiens.api.dto.AvailableResourcePeriodGetDTO;
import com.tiens.api.dto.EnterMeetingRoomCheckDTO;
import com.tiens.api.dto.MeetingRoomCreateDTO;
import com.tiens.api.dto.MeetingRoomUpdateDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.AvailableResourcePeriodVO;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RpcMeetingRoomServiceImpl implements RpcMeetingRoomService {

    @Autowired
    MeetingConfig meetingConfig;

    /**
     * 前端获取认证资质
     *
     * @param userId
     * @return
     */
    @Override
    public CommonResult<VMMeetingCredentialVO> getCredential(String userId) {
        Long expireTime = System.currentTimeMillis() / 1000 + meetingConfig.getExpireSeconds();
        String nonce = RandomUtil.randomString(40);
        String data = meetingConfig.getAppId() + ":" + userId + ":" + expireTime + ":" + nonce;
        String authorization = HmacSHA256.encode(data, meetingConfig.getAppKey());
        VMMeetingCredentialVO vmMeetingCredentialVO = new VMMeetingCredentialVO();
        vmMeetingCredentialVO.setSignature(authorization);
        vmMeetingCredentialVO.setExpireTime(expireTime);
        vmMeetingCredentialVO.setNonce(nonce);
        vmMeetingCredentialVO.setUserId(userId);
        return CommonResult.success(vmMeetingCredentialVO);
    }

    /**
     * 加入会议前置校验
     *
     * @param enterMeetingRoomCheckDTO
     * @return
     */
    @Override
    public CommonResult enterMeetingRoomCheck(EnterMeetingRoomCheckDTO enterMeetingRoomCheckDTO) {
        return null;
    }

    /**
     * 获取空闲资源列表
     *
     * @param imUserId
     * @return
     */
    @Override
    public CommonResult<List<MeetingResourceVO>> getFreeResourceList(String imUserId) {
        return null;
    }

    /**
     * 创建会议
     *
     * @param meetingRoomCreateDTO
     * @return
     */
    @Override
    public CommonResult createMeetingRoom(MeetingRoomCreateDTO meetingRoomCreateDTO) {
        return null;
    }

    /**
     * 编辑会议
     *
     * @param meetingRoomUpdateDTO
     * @return
     */
    @Override
    public CommonResult updateMeetingRoom(MeetingRoomUpdateDTO meetingRoomUpdateDTO) {
        return null;
    }

    /**
     * 查询会议详情
     *
     * @param meetingRoomId
     * @return
     */
    @Override
    public CommonResult<MeetingRoomDetailDTO> getMeetingRoom(Long meetingRoomId) {
        return null;
    }

    /**
     * 取消会议
     *
     * @param meetingRoomId
     * @return
     */
    @Override
    public CommonResult cancelMeetingRoom(Long meetingRoomId) {
        return null;
    }

    /**
     * 首页查询即将召开和进行中的会议列表
     *
     * @return
     */
    @Override
    public CommonResult<List<MeetingRoomDetailDTO>> getFutureAndRunningMeetingRoomList() {
        return null;
    }

    /**
     * 首页查询历史30天的会议列表
     *
     * @return
     */
    @Override
    public CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList() {
        return null;
    }

    /**
     * 查询资源可用的时间段
     *
     * @param availableResourcePeriodGetDTO
     * @return
     */
    @Override
    public CommonResult<List<AvailableResourcePeriodVO>> getAvailableResourcePeriod(
        AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO) {
        return null;
    }
}
