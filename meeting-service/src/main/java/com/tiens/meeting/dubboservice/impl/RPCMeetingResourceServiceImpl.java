package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tiens.api.dto.CancelResourceAllocateDTO;
import com.tiens.api.dto.ResourceAllocateDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import common.enums.MeetingResourceStateEnum;
import common.enums.MeetingRoomStateEnum;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/23
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RPCMeetingResourceServiceImpl implements RPCMeetingResourceService {

    @Reference(version = "1.0")
    DubboCommonUserService dubboCommonUserService;

    private final MeetingResourceDaoService meetingResourceDaoService;
    private final MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    private final HwMeetingCommonService hwMeetingCommonService;
    private final RpcMeetingUserService rpcMeetingUserService;

    public static void main(String[] args) {
        ZoneId zoneId1 = ZoneId.of("GMT+09:00");
        ZoneId zoneId2 = ZoneId.of("GMT+07:00");
        Instant now = Instant.now();
        Date date = new Date();

        DateTime dateTime = DateUtil.convertTimeZone(date, zoneId1);

        System.out.println(dateTime);
//        ZonedDateTime zonedDateTime = now.atZone(zoneId1);
//        System.out.println(zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        System.out.println(now);
//        System.out.println(zonedDateTime);
    }

    /**
     * 分页查询会议资源列表
     *
     * @return
     */
    @Override
    public CommonResult<List<MeetingResourceVO>> queryMeetingResourceList() throws ServiceException {
        List<MeetingResourcePO> list =
            meetingResourceDaoService.lambdaQuery().orderByDesc(MeetingResourcePO::getSize).list();
        List<MeetingResourceVO> resourceVOS = list.stream().map(t -> {
            MeetingResourceVO meetingResourceVO = BeanUtil.copyProperties(t, MeetingResourceVO.class);
            meetingResourceVO.setResourceType(String.valueOf(t.getResourceType()));
            return meetingResourceVO;
        }).collect(Collectors.toList());

        return CommonResult.success(resourceVOS);
    }

    /**
     * 分配
     *
     * @param resourceAllocateDTO
     * @return
     */
    @Override
    public CommonResult allocate(ResourceAllocateDTO resourceAllocateDTO) {
        CommonResult<VMUserVO> vmUserVOCommonResult =
            rpcMeetingUserService.queryVMUser(resourceAllocateDTO.getJoyoCode(), "");
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVO)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_HOST_INFO);
        }
        //共有空闲、共有预约可分配，其他状态都不可分配
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceAllocateDTO.getResourceId());
        Integer status = meetingResourcePO.getStatus();
        if (MeetingResourceStateEnum.PRIVATE.getState()
            .equals(status) || MeetingResourceStateEnum.REDISTRIBUTION.getState().equals(status)) {
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_ALLOCATE_RESOURCE);
        }
        //当前资源状态是否为公有空闲
        Boolean freeFlag = MeetingResourceStateEnum.PUBLIC_FREE.getState().equals(status);
        meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceAllocateDTO.getResourceId())
            .set(MeetingResourcePO::getStatus, freeFlag ? MeetingResourceStateEnum.PRIVATE.getState()
                : MeetingResourceStateEnum.REDISTRIBUTION.getState())
            .set(freeFlag, MeetingResourcePO::getCurrentUseImUserId, vmUserVO.getAccid())
            .set(MeetingResourcePO::getOwnerImUserId, vmUserVO.getAccid())
            .set(MeetingResourcePO::getOwnerImUserJoyoCode, vmUserVO.getJoyoCode())
            .set(MeetingResourcePO::getOwnerImUserName, vmUserVO.getNickName()).update();
        if (freeFlag) {
            //当前是空闲状态，则直接分配资源
            hwMeetingCommonService.associateVmr(vmUserVO.getAccid(),
                Collections.singletonList(meetingResourcePO.getVmrId()));
        }
        return CommonResult.success(null);
    }

    /**
     * 取消分配
     *
     * @param cancelResourceAllocateDTO
     * @return
     */
    @Override
    public CommonResult cancelAllocate(CancelResourceAllocateDTO cancelResourceAllocateDTO) {
        //共有空闲、共有预约可分配，其他状态都不可分配
        Integer resourceId = cancelResourceAllocateDTO.getResourceId();
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
        Integer status = meetingResourcePO.getStatus();
        if (MeetingResourceStateEnum.PUBLIC_FREE.getState()
            .equals(status) || MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState().equals(status)) {
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_CANCEL_ALLOCATE_RESOURCE);
        }
        Boolean privateFlag = MeetingResourceStateEnum.PRIVATE.getState().equals(status);
        //查询是否有进行中或者预约中的会议
        if (privateFlag) {
            List<MeetingRoomInfoPO> meetingRoomInfoPOList =
                meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                    .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).list();
            Boolean emptyRoomFlag = CollectionUtil.isEmpty(meetingRoomInfoPOList);
            if (privateFlag && !emptyRoomFlag) {
                //存在会议，无法取消分配，需要先处理会议
                log.error("私有资源存在进行中或者预约中的会议，无法取消分配");
                return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_CANCEL_ALLOCATE_RESOURCE);
            }
        }
        //可以取消分配
        meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceId)
            .set(MeetingResourcePO::getOwnerImUserId, null).set(MeetingResourcePO::getOwnerImUserJoyoCode, null)
            .set(MeetingResourcePO::getOwnerImUserName, null)
            .set(privateFlag, MeetingResourcePO::getCurrentUseImUserId, null).set(MeetingResourcePO::getStatus,
                privateFlag ? MeetingResourceStateEnum.PUBLIC_FREE.getState()
                    : MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState()).update();
        return CommonResult.success(null);
    }

    /**
     * 根据资源号查询会议列表
     *
     * @param resourceId
     * @return
     */
    @Override
    public CommonResult<List<MeetingRoomDetailDTO>> queryMeetingRoomList(Integer resourceId) {
        List<MeetingRoomInfoPO> list =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();
        List<MeetingRoomDetailDTO> meetingRoomDetailDTOS = BeanUtil.copyToList(list, MeetingRoomDetailDTO.class);
        return CommonResult.success(meetingRoomDetailDTOS);
    }
}
