package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiens.api.dto.*;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import common.enums.MeetingNewResourceStateEnum;
import common.enums.MeetingNewRoomTypeEnum;
import common.enums.MeetingRoomStateEnum;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import common.util.cache.CacheKeyUtil;
import common.util.date.DateUtils;
import common.util.io.ExcelUtil;
import common.util.servlet.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
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


    private final MeetingResourceDaoService meetingResourceDaoService;
    private final MeetingRoomInfoDaoService meetingRoomInfoDaoService;
    private final MeetingAttendeeDaoService meetingAttendeeDaoService;


    private final HwMeetingCommonService hwMeetingCommonService;
    private final RpcMeetingUserService rpcMeetingUserService;

    private final RedissonClient redissonClient;

    private static final ObjectMapper sObjectMapper = new ObjectMapper();

    @Value("${excel.max-number:3000}")
    private int maxNumber;

    public static void main(String[] args) {
        System.out.println(LocalDateTimeUtil.now());
        ZoneId zoneId1 = ZoneId.of("UTC");
        ZoneId zoneId2 = ZoneId.of("GMT+07:00");
        Instant now = Instant.now();
        Date date = new Date();
        DateTime dateTime = DateUtil.convertTimeZone(date, zoneId1);
//        System.out.println(dateTime);
//        ZonedDateTime zonedDateTime = now.atZone(zoneId1);
//        System.out.println(zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

//        System.out.println(now);
//        System.out.println(zonedDateTime);


        // 原始日期
        Date originalDate = new Date();
        TimeZone userTimeZone = TimeZone.getTimeZone(ZoneId.of("GMT+08:00"));
        TimeZone zeroTimeZone = TimeZone.getTimeZone(ZoneId.of("GMT"));
        int timeZoneOffset = userTimeZone.getRawOffset() - zeroTimeZone.getRawOffset();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(originalDate);
        calendar.add(Calendar.MILLISECOND, timeZoneOffset);
        Date newDate = calendar.getTime();

//        System.out.println("新日期： " + newDate);

        System.out.println(DateUtils.convertTimeZone(originalDate, ZoneId.of("GMT+08:00"), ZoneId.of("GMT")));
    }

    /**
     * 分页查询会议资源列表
     *
     * @return
     */
    @Override
    public CommonResult<List<MeetingResourceVO>> queryMeetingResourceList(MeetingResourceQueryDTO meetingResourceQueryDTO) throws ServiceException {
        List<MeetingResourcePO> list =
            meetingResourceDaoService.lambdaQuery()
                    .eq(StringUtils.isNotBlank(meetingResourceQueryDTO.getVmrName()),MeetingResourcePO::getVmrName,meetingResourceQueryDTO.getVmrName())
                    .eq(meetingResourceQueryDTO.getSize()!=null,MeetingResourcePO::getSize,meetingResourceQueryDTO.getSize())
                    .eq(meetingResourceQueryDTO.getMeetingRoomType()!=null,MeetingResourcePO::getMeetingRoomType,meetingResourceQueryDTO.getMeetingRoomType())
                    .eq(meetingResourceQueryDTO.getResourceStatus()!=null,MeetingResourcePO::getResourceStatus,meetingResourceQueryDTO.getResourceStatus())
                    .orderByAsc(MeetingResourcePO::getSize).list();
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
    @Transactional
    @Override
    public CommonResult allocate(ResourceAllocateDTO resourceAllocateDTO) {
        log.info("【分配资源】入参为：{}", resourceAllocateDTO);
        CommonResult<VMUserVO> vmUserVOCommonResult =
            rpcMeetingUserService.queryVMUser(resourceAllocateDTO.getJoyoCode(), "");
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVO)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_HOST_INFO);
        }
        //添加华为用户,防止分配资源失败
        rpcMeetingUserService.addMeetingCommonUser(vmUserVO.getAccid());

        Integer resourceId = resourceAllocateDTO.getResourceId();

        RLock lock = redissonClient.getLock(CacheKeyUtil.getResourceLockKey(resourceId));
        try {
            if (lock.isLocked()) {
                //资源锁定中
                log.error("【分配资源】抢占资源锁失败，资源已被占用，资源id:{}", resourceId);
                return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_OPERATED_ERROR);
            }
            //资源维度锁定
            lock.lock(10, TimeUnit.SECONDS);

            //预分配或私人专属不可再分配
            MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
            Integer status = meetingResourcePO.getResourceStatus();
            Integer type = meetingResourcePO.getMeetingRoomType();
            if (MeetingNewRoomTypeEnum.PRIVATE.getState()
                .equals(type) || MeetingNewResourceStateEnum.SUBSCRIBE.getState()
                    .equals(meetingResourcePO.getPreAllocation())) {
                return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_ALLOCATE_RESOURCE);
            }
            //当前资源状态是否为公有空闲
            boolean freeFlag = MeetingNewResourceStateEnum.FREE.getState().equals(status) && MeetingNewRoomTypeEnum.PUBLIC.getState().equals(type);
            meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceAllocateDTO.getResourceId())
                .set(MeetingResourcePO::getMeetingRoomType, MeetingNewRoomTypeEnum.PRIVATE.getState())
                .set(!freeFlag,MeetingResourcePO::getPreAllocation, MeetingNewResourceStateEnum.SUBSCRIBE.getState())
                .set(freeFlag, MeetingResourcePO::getCurrentUseImUserId, vmUserVO.getAccid())
                .set(MeetingResourcePO::getOwnerImUserId, vmUserVO.getAccid())
                .set(MeetingResourcePO::getOwnerImUserJoyoCode, vmUserVO.getJoyoCode())
                .set(MeetingResourcePO::getOwnerImUserName, vmUserVO.getNickName())
                .set(MeetingResourcePO::getOwnerExpireDate, resourceAllocateDTO.getOwnerExpireDate()).update();
            if (freeFlag) {
                //当前是空闲状态，则直接分配资源
                hwMeetingCommonService.associateVmr(vmUserVO.getAccid(),
                    Collections.singletonList(meetingResourcePO.getVmrId()));
            }
            return CommonResult.success(null);
        } catch (Exception e) {
            log.error("【分配资源】发生异常，资源id：{},异常：{}", resourceId, e);
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.info("【分配资源】释放资源锁：资源id：{}", resourceId);
                lock.unlock();
            }
        }
    }

    /**
     * 取消分配
     *
     * @param cancelResourceAllocateDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult cancelAllocate(CancelResourceAllocateDTO cancelResourceAllocateDTO) {
        //共有空闲、共有预约可分配，其他状态都不可分配
        Integer resourceId = cancelResourceAllocateDTO.getResourceId();

        RLock lock = redissonClient.getLock(CacheKeyUtil.getResourceLockKey(resourceId));
        try {
            if (lock.isLocked()) {
                //资源锁定中
                log.error("【取消分配资源】抢占资源锁失败，资源已被占用，资源id:{}", resourceId);
                return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_OPERATED_ERROR);
            }
            //资源维度锁定
            lock.lock(10, TimeUnit.SECONDS);

            MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
            Integer status = meetingResourcePO.getResourceStatus();
            Integer roomType = meetingResourcePO.getMeetingRoomType();
            //非私有资源 且不存在预分配
            if(!MeetingNewRoomTypeEnum.PRIVATE.getState().equals(roomType) && MeetingNewResourceStateEnum.FREE.getState().equals(meetingResourcePO.getPreAllocation())){
                return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_CANCEL_ALLOCATE_RESOURCE);
            }

            Boolean privateFlag = MeetingNewRoomTypeEnum.PRIVATE.getState().equals(roomType);
            //查询是否有进行中或者预约中的会议
            if (privateFlag) {
                List<MeetingRoomInfoPO> meetingRoomInfoPOList =
                    meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                        .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).list();
                Boolean emptyRoomFlag = CollectionUtil.isEmpty(meetingRoomInfoPOList);
                if (privateFlag && !emptyRoomFlag) {
                    //存在会议，无法取消分配，需要先处理会议
                    log.error("【取消分配资源】私有资源存在进行中或者预约中的会议，无法取消分配");
                    return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_CANCEL_ALLOCATE_RESOURCE);
                }
            }
            //可以取消分配
            meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceId)
                .set(MeetingResourcePO::getOwnerImUserId, null)
                .set(MeetingResourcePO::getOwnerImUserJoyoCode, null)
                .set(MeetingResourcePO::getOwnerImUserName, null)
                .set(privateFlag, MeetingResourcePO::getCurrentUseImUserId, null)
                .set(privateFlag,MeetingResourcePO::getMeetingRoomType,MeetingNewRoomTypeEnum.INIT.getState())
                .set(MeetingResourcePO::getResourceStatus,MeetingNewResourceStateEnum.FREE.getState())
                .set(MeetingResourcePO::getPreAllocation,MeetingNewResourceStateEnum.FREE.getState())
                .set(MeetingResourcePO::getOwnerExpireDate,null)
                .update();
            return CommonResult.success(null);
        } catch (Exception e) {
            log.error("【取消分配资源】发生异常，资源id：{},异常：{}", resourceId, e);
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.info("【取消分配资源】释放资源锁：资源id：{}", resourceId);
                lock.unlock();
            }
        }
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

    @Override
    public CommonResult<PageResult<MeetingRoomInfoDTO>> queryMeetingRoomPage(PageParam<MeetingRoomInfoQueryDTO> query) {
        if (query.getCondition().isExport()) {
            // 限制100页
            query.setPageSize(maxNumber * 100);
            IPage<MeetingRoomInfoDTO> page = meetingRoomInfoDaoService.queryPage(query);
            try {
                ExcelUtil.downloadExcel(ServletUtils.getResponse(), sObjectMapper.readTree(sObjectMapper.writeValueAsString(page.getRecords())),
                        Arrays.asList("资源ID", "云会议号", "资源名称",
                                "资源类型", "会议状态", "会议室类型",
                                "资源大小", "预约时间", "预约人", "预约人ID",
                                "所在区域", "计划开始时间", "时长",
                                "实际开始时间", "实际结束时间", "与会人数"),
                        Arrays.asList("resourceId", "hwMeetingCode", "resourceName",
                                "resourceTypeDesc", "state", "meetingRoomTypeDesc",
                                "size", "createTime", "ownerUserName", "ownerImUserId",
                                "area", "showStartTime", "duration",
                                "relStartTime", "relEndTime", "persons"
                        ), "会议列表");
            } catch (IOException e) {
                log.error("export error:{}", e.getMessage());
            }
            return null;
        }
        IPage<MeetingRoomInfoDTO> page = meetingRoomInfoDaoService.queryPage(query);
        List<MeetingRoomInfoDTO> records = page.getRecords();
        List<String> roomIds = records.stream().map(MeetingRoomInfoDTO::getId).collect(Collectors.toList());
        // 获取参会人数
        if (roomIds.size() > 0) {
            List<Map<String, Object>> roomList = meetingAttendeeDaoService.queryPersonsByRoomIds(roomIds);
            records.forEach((item) -> {
                Optional<Object> first = roomList.stream()
                        .filter(room -> room.get("roomId").equals(item.getId()))
                        .map(room -> room.get("persons"))
                        .findFirst();
                first.ifPresent(o -> item.setPersons(Integer.parseInt(String.valueOf(o))));
            });
        }
        PageResult<MeetingRoomInfoDTO> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(page.getTotal());
        return CommonResult.success(pageResult);
    }

}
