package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.huaweicloud.sdk.meeting.v1.utils.HmacSHA256;
import com.tiens.api.dto.*;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.AvailableResourcePeriodVO;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.MeetingLevelResourceConfigPO;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import com.tiens.meeting.repository.service.MeetingLevelResourceConfigDaoService;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import common.enums.MeetingRoomStateEnum;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private final MeetingConfig meetingConfig;

    private final MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    private final MeetingResourceDaoService meetingResourceDaoService;

    private final MeetingLevelResourceConfigDaoService meetingLevelResourceConfigDaoService;

    private final MeetingHostUserDaoService meetingHostUserDaoService;

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
//        若不在则 tos 提示，请在 XXXX年XX月XX日 XX:XX后进入会议。
        String meetRoomCode = enterMeetingRoomCheckDTO.getMeetRoomCode();
        //查询会议code是否存在
        Optional<MeetingRoomInfoPO> meetingRoomInfoPOOpt =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getHwMeetingCode, meetRoomCode).oneOpt();
        if (!meetingRoomInfoPOOpt.isPresent()) {
            //不存在会议
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_ROOM_INFO);
        }
        // 若为共有资源会议，需判断是否为开始时间 30min内，若在则直接进入会议，
        MeetingRoomInfoPO meetingRoomInfoPO = meetingRoomInfoPOOpt.get();
        Integer resourceId = meetingRoomInfoPO.getResourceId();
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
        if (ObjectUtil.isEmpty(meetingResourcePO.getOwnerImUserId())) {
            //此资源为共有资源
            Date lockStartTime = meetingRoomInfoPO.getLockStartTime();
            DateTime now = DateUtil.date();
            if (now.isBefore(lockStartTime)) {
                //未到开会开始时间
                String betweenDate = DateUtil.formatBetween(now, lockStartTime, BetweenFormatter.Level.MINUTE);
                //未到开会开始时间
                return CommonResult.errorMsg(String.format("请在 %s后进入会议", betweenDate));
            }
        }

        return CommonResult.success(null);
    }

    /**
     * 获取空闲资源列表
     *
     * @param freeResourceListDTO
     * @return
     */
    @Override
    public CommonResult<List<MeetingResourceVO>> getFreeResourceList(FreeResourceListDTO freeResourceListDTO) {
        //前端用户能看到的资源列表=【公池该用户等级相关空闲子资源与主持人绑定公池空闲资源 的【并集】】+用户私池

        ArrayList<@Nullable MeetingResourceVO> finalResourceList = Lists.newArrayList();

        //1、查询用户私池资源
        List<MeetingResourceVO> privateResourceList = getPrivateResourceList(freeResourceListDTO);

        //2、查询用户公池资源
        List<MeetingResourceVO> publicResourceList = getPublicResourceList(freeResourceListDTO);
        finalResourceList.addAll(privateResourceList);
        finalResourceList.addAll(publicResourceList);
        return CommonResult.success(finalResourceList);
    }

    private List<MeetingResourceVO> getPrivateResourceList(FreeResourceListDTO imUserId) {
        List<MeetingResourcePO> list =
            meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getOwnerImUserId, imUserId).list();
        return BeanUtil.copyToList(list, MeetingResourceVO.class);
    }

    private List<MeetingResourceVO> getPublicResourceList(FreeResourceListDTO freeResourceListDTO) {
        //公池该用户等级相关空闲子资源与主持人绑定公池空闲资源 的【并集】
        //查询当前时段所有被使用的资源列表
        DateTime startTime = DateUtil.date(freeResourceListDTO.getStartTime());
        DateTime endTime = DateUtil.date(startTime).offset(DateField.MINUTE, freeResourceListDTO.getLength());
        Consumer<LambdaQueryWrapper<MeetingRoomInfoPO>> consumer =
            wrapper -> wrapper.ge(MeetingRoomInfoPO::getLockStartTime, startTime)
                .le(MeetingRoomInfoPO::getLockStartTime, endTime)
                .or(wrapper1 -> wrapper1.ge(MeetingRoomInfoPO::getLockEndTime, startTime)
                    .le(MeetingRoomInfoPO::getLockEndTime, endTime))
                .or(wrapper2 -> wrapper2.le(MeetingRoomInfoPO::getLockStartTime, startTime)
                    .ge(MeetingRoomInfoPO::getLockEndTime, endTime));
        //该段时间正在锁定的会议
        List<MeetingRoomInfoPO> lockedMeetingRoomList = meetingRoomInfoDaoService.lambdaQuery()
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).nested(consumer).list();
        //该段时间正在锁定的资源
        List<Integer> lockedResourceIdList =
            lockedMeetingRoomList.stream().map(MeetingRoomInfoPO::getResourceId).collect(Collectors.toList());

        //根据等级查询资源
        MeetingLevelResourceConfigPO one = meetingLevelResourceConfigDaoService.lambdaQuery()
            .select(MeetingLevelResourceConfigPO::getResourceType)
            .eq(MeetingLevelResourceConfigPO::getVmUserLevel, freeResourceListDTO.getLevelCode())
            .one();
        Integer maxResourceType = one.getResourceType();
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
