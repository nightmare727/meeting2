package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.*;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.huaweicloud.sdk.meeting.v1.utils.HmacSHA256;
import com.tiens.api.dto.*;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.*;
import com.tiens.meeting.dubboservice.async.RoomAsyncTaskService;
import com.tiens.meeting.dubboservice.common.NewComerTasks;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import com.tiens.meeting.repository.config.DataCache;
import com.tiens.meeting.repository.po.*;
import com.tiens.meeting.repository.service.*;
import com.tiens.meeting.util.FreeTimeCalculatorUtil;
import com.tiens.meeting.util.WheelTimerContext;
import common.enums.*;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.util.cache.CacheKeyUtil;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RLock;
import org.redisson.api.RLongAdder;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private final MeetingTimeZoneConfigDaoService meetingTimeZoneConfigDaoService;

    private final Map<String, HwMeetingRoomHandler> hwMeetingRoomHandlers;

    private final HwMeetingCommonService hwMeetingCommonService;

    private final RedissonClient redissonClient;

    private final RoomAsyncTaskService roomAsyncTaskService;

    private final MeetingAttendeeDaoService meetingAttendeeDaoService;

    private final DataCache dataCache;

    public static final String privateResourceTypeFormat = "专属会议室（适用于%d人以下）";

    ListeningExecutorService listeningExecutorService =
        MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    /**
     * 前端获取认证资质
     *
     * @param userId
     * @return
     */
    @Override
    public CommonResult<VMMeetingCredentialVO> getCredential(String userId) {
        Integer expireTime = Math.toIntExact(DateUtil.currentSeconds() + meetingConfig.getExpireSeconds());
        String nonce = RandomUtil.randomString(40);
        String data = meetingConfig.getAppId() + ":" + userId + ":" + expireTime + ":" + nonce;
        String authorization = HmacSHA256.encode(data, meetingConfig.getAppKey());
        VMMeetingCredentialVO vmMeetingCredentialVO = new VMMeetingCredentialVO();
        vmMeetingCredentialVO.setSignature(authorization);
        vmMeetingCredentialVO.setExpireTime(Math.toIntExact(expireTime));
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
        // 若不在则 tos 提示，请在 XXXX年XX月XX日 XX:XX后进入会议。
        String meetRoomCode = enterMeetingRoomCheckDTO.getMeetRoomCode();
        String timeZoneOffset = enterMeetingRoomCheckDTO.getTimeZoneOffset();
        log.info("加入会议校验入参：{}", enterMeetingRoomCheckDTO);
        // 查询会议code是否存在

        List<MeetingRoomInfoPO> list =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getHwMeetingCode, meetRoomCode)
                .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();

        if (CollectionUtil.isEmpty(list)) {
            // 不存在会议
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_ROOM_INFO);
        }

        // 若为共有资源会议，需判断是否为开始时间 30min内，若在则直接进入会议，
        MeetingRoomInfoPO meetingRoomInfoPO = list.get(0);

        String state = meetingRoomInfoPO.getState();
        if (MeetingRoomStateEnum.Destroyed.getState().equals(state)) {
            // 会议已结束
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_ROOM_INFO);
        }

        // 此资源为共有资源
        Date lockStartTime = meetingRoomInfoPO.getLockStartTime();
        DateTime now = DateUtil.date();
        if (now.isBefore(lockStartTime)) {
            // 未到开会开始时间
            return CommonResult.error(GlobalErrorCodeConstants.NOT_ARRIVE_START_TIME_ERROR.getCode(),
                String.format(GlobalErrorCodeConstants.NOT_ARRIVE_START_TIME_ERROR.getChinesMsg(),
                    DateUtils.convertTimeZone(lockStartTime, DateUtils.TIME_ZONE_GMT, ZoneId.of(timeZoneOffset))));
            // return CommonResult.errorMsg(String.format("请在 %s后进入会议", betweenDate));
        }
        log.info("加入会议校验通过：主持人id:{}", meetingRoomInfoPO.getOwnerImUserId());
        // 返回主持人的id
        return CommonResult.success(meetingRoomInfoPO.getOwnerImUserId());
    }

    /**
     * 加入会议
     *
     * @param joinMeetingRoomDTO
     * @return
     */
    @Override
    @NewComerTasks
    public CommonResult enterMeetingRoom(JoinMeetingRoomDTO joinMeetingRoomDTO) {
        log.info("【加入会议】 入参：{}", JSON.toJSONString(joinMeetingRoomDTO));
        String meetRoomCode = joinMeetingRoomDTO.getMeetRoomCode();
        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery().and(
                wrapper1 -> wrapper1.eq(MeetingRoomInfoPO::getHwMeetingCode, meetRoomCode)
                    .or(wrapper2 -> wrapper2.eq(MeetingRoomInfoPO::getConferenceId, meetRoomCode)))
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
            .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();

        if (ObjectUtil.isEmpty(list)) {
            log.error("【加入会议】 会议不存在，会议号：{}", joinMeetingRoomDTO.getMeetRoomCode());
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_ROOM_INFO);
        }
        MeetingRoomInfoPO meetingRoomInfoPO = list.get(0);
        MeetingAttendeePO exMeetingAttendeePO =
            meetingAttendeeDaoService.lambdaQuery().eq(MeetingAttendeePO::getMeetingRoomId, meetingRoomInfoPO.getId())
                .eq(MeetingAttendeePO::getAttendeeUserId, joinMeetingRoomDTO.getImUserId()).one();
        if (ObjectUtil.isNotNull(exMeetingAttendeePO) && MeetingUserJoinStatusEnum.UN_JOINED.getCode()
            .equals(exMeetingAttendeePO.getJoinStatus())) {
            // 更新与会者入会状态
            meetingAttendeeDaoService.lambdaUpdate().eq(MeetingAttendeePO::getId, exMeetingAttendeePO.getId())
                .set(MeetingAttendeePO::getJoinStatus, MeetingUserJoinStatusEnum.JOINED.getCode()).update();

        } else {
            MeetingAttendeePO meetingAttendeePO = new MeetingAttendeePO();
            meetingAttendeePO.setMeetingRoomId(meetingRoomInfoPO.getId());
            meetingAttendeePO.setAttendeeUserId(joinMeetingRoomDTO.getImUserId());
            meetingAttendeePO.setAttendeeUserName(joinMeetingRoomDTO.getAttendeeUserName());
            meetingAttendeePO.setSource(MeetingUserJoinSourceEnum.MIDWAY.getCode());
            meetingAttendeePO.setJoinStatus(MeetingUserJoinStatusEnum.JOINED.getCode());
            try {
                meetingAttendeeDaoService.save(meetingAttendeePO);
            } catch (DuplicateKeyException e) {
                log.info("【加入会议】 重复添加会议与会者");
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

        log.info("空闲资源列表【0】入参：{}", freeResourceListDTO);
        Integer leadTime = freeResourceListDTO.getLeadTime();

        if (ObjectUtil.isEmpty(freeResourceListDTO.getTimeZoneOffset())) {
            freeResourceListDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
            if (ObjectUtil.isNotEmpty(freeResourceListDTO.getStartTime())) {
                // 时区为空,则为东8
                freeResourceListDTO.setStartTime(DateUtil.offsetHour(freeResourceListDTO.getStartTime(), -8));
            }
        }
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);

        Date showStartTime =
            DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(freeResourceListDTO.getStartTime(), now),
                DateUtils.TIME_ZONE_GMT);
        DateTime lockStartTime = DateUtil.offsetMinute(showStartTime, -leadTime);
        DateTime lockEndTime = DateUtil.offsetMinute(showStartTime, freeResourceListDTO.getLength() + 29);
        // 前端用户能看到的资源列表=【公池该用户等级相关空闲子资源与主持人绑定公池空闲资源 的【并集】】+用户私池
        List<MeetingResourceVO> result;
        if (NumberUtil.isNumber(freeResourceListDTO.getResourceType())) {
            // 2、查询用户公池资源
            result = getPublicResourceList(freeResourceListDTO);
        } else {
            // 1、查询用户私池资源
            result = getPrivateResourceList(freeResourceListDTO);
        }
//        log.info("空闲资源列表【1】初始过滤资源池结果：{}", result);

        List<Integer> originResourceIds =
            result.stream().filter(t -> t.getExpireDate().after(lockEndTime)).map(MeetingResourceVO::getId)
                .collect(Collectors.toList());

        if (ObjectUtil.isEmpty(originResourceIds)) {
            return CommonResult.success(Collections.emptyList());
        }

        List<MeetingRoomInfoPO> lockedMeetingRoomList =
            getOccupiedMeetingRoom(originResourceIds, lockStartTime, lockEndTime);
//        log.info("空闲资源列表【2】，锁定开始时间：{}，锁定结束时间：{}，查询锁定会议结果：{}", lockStartTime, lockEndTime,
//            lockedMeetingRoomList);

        // 该段时间正在锁定的资源
        List<Integer> lockedResourceIdList =
            lockedMeetingRoomList.stream().map(MeetingRoomInfoPO::getResourceId).collect(Collectors.toList());
        // 去除空闲资源中被锁定的资源
        result = result.stream()
            .filter(t -> originResourceIds.contains(t.getId()) && !lockedResourceIdList.contains(t.getId()))
            .peek(t -> t.setResourceType(freeResourceListDTO.getResourceType())).collect(Collectors.toList());
//        log.info("空闲资源列表结果：{}", result);
        return CommonResult.success(result);
    }

    /**
     * 查询资源被占用的会议列表
     *
     * @param resourceIdList
     * @param lockStartTime
     * @param lockEndTime
     * @return
     */
    List<MeetingRoomInfoPO> getOccupiedMeetingRoom(List<Integer> resourceIdList, DateTime lockStartTime,
        DateTime lockEndTime) {
        if (CollectionUtil.isEmpty(resourceIdList)) {
            return Collections.emptyList();
        }
        Consumer<LambdaQueryWrapper<MeetingRoomInfoPO>> consumer =
            wrapper -> wrapper.ge(MeetingRoomInfoPO::getLockStartTime, lockStartTime)
                .le(MeetingRoomInfoPO::getLockStartTime, lockEndTime)
                .or(wrapper1 -> wrapper1.ge(MeetingRoomInfoPO::getLockEndTime, lockStartTime)
                    .le(MeetingRoomInfoPO::getLockEndTime, lockEndTime))
                .or(wrapper2 -> wrapper2.le(MeetingRoomInfoPO::getLockStartTime, lockStartTime)
                    .ge(MeetingRoomInfoPO::getLockEndTime, lockEndTime));
        // 该段时间正在锁定的会议
        List<MeetingRoomInfoPO> lockedMeetingRoomList =
            meetingRoomInfoDaoService.lambdaQuery().in(MeetingRoomInfoPO::getResourceId, resourceIdList)
                .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).nested(consumer)
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();
        return lockedMeetingRoomList;
    }

    private List<MeetingResourceVO> getPrivateResourceList(FreeResourceListDTO freeResourceListDTO) {
        String resourceType = freeResourceListDTO.getResourceType();
        String[] split = resourceType.split("-");
        String userId = split[0];
        String size = split[1];
        String relType = split[2];
        List<MeetingResourcePO> list = meetingResourceDaoService.lambdaQuery()
            .eq(MeetingResourcePO::getOwnerImUserId, freeResourceListDTO.getImUserId())
            .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState())
            .eq(MeetingResourcePO::getResourceType, relType).list();
        return BeanUtil.copyToList(list, MeetingResourceVO.class);
    }

    /**
     * 查询所有可用的资源
     *
     * @param freeResourceListDTO
     * @return
     */
    private List<MeetingResourceVO> getPublicResourceList(FreeResourceListDTO freeResourceListDTO) {
        // 公池该用户等级相关空闲子资源与主持人绑定公池空闲资源 的【并集】
        // 查询当前时段所有被使用的资源列表

        // 根据资源类型查询所有空闲资源
        List<MeetingResourcePO> levelFreeResourceList = meetingResourceDaoService.lambdaQuery()
            .notIn(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState(),
                MeetingResourceStateEnum.REDISTRIBUTION.getState())
            .eq(MeetingResourcePO::getResourceType, Integer.parseInt(freeResourceListDTO.getResourceType())).list();

        return BeanUtil.copyToList(levelFreeResourceList, MeetingResourceVO.class);
    }

    /**
     * 获取最大会议用户等级
     *
     * @param levelCode
     * @param imUserId
     * @param nationId
     * @return
     */
    public Integer getMaxLevel(Integer levelCode, String imUserId, String nationId) {
        if ("CN".equals(nationId)) {
            Optional<MeetingLevelResourceConfigPO> meetingLevelResourceConfigPO =
                meetingLevelResourceConfigDaoService.lambdaQuery().select(MeetingLevelResourceConfigPO::getResourceType)
                    .orderByDesc(MeetingLevelResourceConfigPO::getResourceNum).last(" limit 1").oneOpt();
            if (meetingLevelResourceConfigPO.isPresent()) {
                return meetingLevelResourceConfigPO.get().getResourceType();
            } else {
                //默认
                int defaultMax = 9;
                return defaultMax;
            }

        }

        // 根据等级查询资源
        MeetingLevelResourceConfigPO one =
            meetingLevelResourceConfigDaoService.lambdaQuery().select(MeetingLevelResourceConfigPO::getResourceType)
                .eq(MeetingLevelResourceConfigPO::getVmUserLevel, levelCode).one();
        Integer maxResourceType = one.getResourceType();
        // 查询该用户的主持人等级
        Optional<MeetingHostUserPO> meetingHostUserPOOptional =
            meetingHostUserDaoService.lambdaQuery().eq(MeetingHostUserPO::getAccId, imUserId)
                .select(MeetingHostUserPO::getResourceType).oneOpt();
        if (meetingHostUserPOOptional.isPresent()) {
            // 比较和等级关联的资源类型大小
            maxResourceType = NumberUtil.max(meetingHostUserPOOptional.get().getResourceType(), maxResourceType);
        }
        return maxResourceType;
    }

    /**
     * 创建会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Transactional
    @Override
    @NewComerTasks
    public CommonResult<MeetingRoomDetailDTO> createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO)
        throws Exception {
        log.info("【创建、预约会议】开始，参数为：{}", meetingRoomContextDTO);
        if (ObjectUtil.isEmpty(meetingRoomContextDTO.getTimeZoneOffset())) {
            meetingRoomContextDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
            if (ObjectUtil.isNotEmpty(meetingRoomContextDTO.getStartTime())) {
                // 时区为空,则为东8
                meetingRoomContextDTO.setStartTime(DateUtil.offsetHour(meetingRoomContextDTO.getStartTime(), -8));
            }
        }

        Integer resourceId = meetingRoomContextDTO.getResourceId();
        RLock lock = redissonClient.getLock(CacheKeyUtil.getResourceLockKey(resourceId));

        MeetingRoomModel meetingRoom = null;
        Integer vmrMode = null;
        MeetingResourcePO meetingResourcePO = null;
        String currentUseImUserId = null;
        try {
            if (lock.isLocked()) {
                // 资源锁定中
                log.error("【创建、预约会议】抢占资源锁失败，资源已被占用，资源id:{}", resourceId);
                return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_OPERATED_ERROR);
            }
            // 资源维度锁定
            lock.lock(10, TimeUnit.SECONDS);
            CommonResult checkResult = checkCreateMeetingRoom(meetingRoomContextDTO);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            Tuple2<MeetingResourcePO, MeetingTimeZoneConfigPO> of =
                (Tuple2<MeetingResourcePO, MeetingTimeZoneConfigPO>)checkResult.getData();

            meetingResourcePO = of.getT1();
            MeetingTimeZoneConfigPO meetingTimeZoneConfigPO = of.getT2();

            meetingRoomContextDTO.setVmrId(meetingResourcePO.getVmrId());
            meetingRoomContextDTO.setVmrMode(meetingResourcePO.getVmrMode());
            meetingRoomContextDTO.setResourceStatus(meetingResourcePO.getStatus());
            meetingRoomContextDTO.setTimeZoneID(meetingTimeZoneConfigPO.getTimeZoneId());

            // 查询是否该资源已分配，
            currentUseImUserId = meetingResourcePO.getCurrentUseImUserId();
            meetingRoomContextDTO.setCurrentResourceUserId(currentUseImUserId);
            // 创建会议
            vmrMode = meetingResourcePO.getVmrMode();

            // 1、创建华为云会议
            meetingRoom = hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                .createMeetingRoom(meetingRoomContextDTO);
            // 包装po实体
            MeetingRoomInfoPO meetingRoomInfoPO =
                packMeetingRoomInfoPO(meetingRoomContextDTO, meetingRoom, meetingResourcePO);
            // 2、创建本地会议
            meetingRoomInfoDaoService.save(meetingRoomInfoPO);
            Long meetingRoomId = meetingRoomInfoPO.getId();
            // 插入与会者数据
            List<MeetingAttendeeDTO> attendees = meetingRoomContextDTO.getAttendees();
            if (ObjectUtil.isNotEmpty(attendees)) {
                DateTime now = DateUtil.date();
                List<MeetingAttendeePO> collect = attendees.stream().map(
                        t -> MeetingAttendeePO.builder().meetingRoomId(meetingRoomId).attendeeUserId(t.getAttendeeUserId())
                            .attendeeUserName(t.getAttendeeUserName()).source(MeetingUserJoinSourceEnum.APPOINT.getCode())
                            .attendeeUserHeadUrl(t.getAttendeeUserHeadUrl()).createTime(now).updateTime(now).build())
                    .distinct().collect(Collectors.toList());

                roomAsyncTaskService.batchSendIMMessage(meetingRoomInfoPO,
                    attendees.stream().map(MeetingAttendeeDTO::getAttendeeUserId).collect(Collectors.toList()));

                meetingAttendeeDaoService.saveBatch(collect);
            }

            // 3、锁定资源，更改资源状态为共有预约
            publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_UP);
            // 返回创建后得详情
            MeetingRoomDetailDTO result = packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, null);
            result.setResourceExpireTime(meetingResourcePO.getExpireDate());
            log.info("【创建、预约会议】完成，参数为：{}", meetingRoomContextDTO);
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("【创建、预约会议】异常", e);
            // 创建华为云会议成功了
            if (ObjectUtil.isNotNull(meetingRoom)) {
                // 取消异常创建的会议
                Integer finalVmrMode = vmrMode;
                MeetingRoomModel finalMeetingRoom = meetingRoom;
                MeetingResourcePO finalMeetingResourcePO = meetingResourcePO;
                String finalCurrentUseImUserId = currentUseImUserId;
                listeningExecutorService.submit(() -> {
                    try {
                        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(finalVmrMode))
                            .cancelMeetingRoom(new CancelMeetingRoomModel(meetingRoomContextDTO.getImUserId(),
                                finalMeetingRoom.getHwMeetingCode(), finalMeetingResourcePO.getVmrId(),
                                NumberUtil.isNumber(meetingRoomContextDTO.getResourceType()), finalCurrentUseImUserId
                                , resourceId));
                    } catch (Exception e1) {
                        log.error("【创建、预约会议】异常取消会议异常，异常信息：{}", e1);
                    }
                });
            }
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.info("【创建、预约会议】释放资源锁：资源id：{}", resourceId);
                lock.unlock();
            }
        }

    }

    private MeetingRoomInfoPO packMeetingRoomInfoPO(MeetingRoomContextDTO meetingRoomContextDTO,
        MeetingRoomModel meetingRoom, MeetingResourcePO meetingResourcePO) {

        Integer resourceId = meetingRoomContextDTO.getResourceId();
        Integer leadTime = meetingRoomContextDTO.getLeadTime();

        // 展示开始时间
        DateTime showStartTime = DateUtils.roundToHalfHour(
            ObjectUtil.defaultIfNull(meetingRoomContextDTO.getStartTime(),
                DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT)), DateUtils.TIME_ZONE_GMT);
        Integer length = meetingRoomContextDTO.getLength();
        // 展示结束时间
        DateTime showEndTime = DateUtil.offsetMinute(showStartTime, length);

        // 锁定开始时间
        DateTime lockStartTime = DateUtil.offsetMinute(showStartTime, -leadTime);
        // 锁定结束时间
        DateTime lockEndTime = DateUtil.offsetMinute(showEndTime, 29);

        String resourceTypeDesc;
        String resourceType = meetingRoomContextDTO.getResourceType();
        Boolean publicFlag = NumberUtil.isNumber(resourceType);

        if (publicFlag) {
            // 共有资源
            resourceTypeDesc = MeetingResourceEnum.getByCode(Integer.parseInt(resourceType)).getDesc();
        } else {
            // 私有资源
            int size = Integer.valueOf(resourceType.split("-")[1]);
            resourceTypeDesc = String.format(privateResourceTypeFormat, size);

        }

        MeetingRoomInfoPO build = MeetingRoomInfoPO.builder().id(meetingRoomContextDTO.getMeetingRoomId())
            .duration(meetingRoomContextDTO.getLength()).showStartTime(showStartTime).showEndTime(showEndTime)
            .lockStartTime(lockStartTime).lockEndTime(lockEndTime).resourceId(resourceId)
            .resourceType(meetingRoomContextDTO.getResourceType()).resourceName(meetingResourcePO.getVmrName())
            .resourceTypeDesc(resourceTypeDesc).ownerImUserId(meetingRoomContextDTO.getImUserId())
            .timeZoneId(meetingRoomContextDTO.getTimeZoneID()).timeZoneOffset(meetingRoomContextDTO.getTimeZoneOffset())
            .vmrMode(meetingRoomContextDTO.getVmrMode()).ownerUserName(meetingRoomContextDTO.getImUserName())
            .subject(meetingRoomContextDTO.getSubject()).remark(meetingRoomContextDTO.getRemark())
            .languageId(meetingRoomContextDTO.getLanguageId()).leadTime(leadTime).build();
        if (ObjectUtil.isNotNull(meetingRoom)) {
            build.setHwMeetingId(meetingRoom.getHwMeetingId());
            build.setHwMeetingCode(meetingRoom.getHwMeetingCode());
            build.setConferenceId(meetingRoom.getConferenceId());
            build.setHostPwd(meetingRoom.getChairmanPwd());
            build.setGeneralPwd(meetingRoom.getGeneralPwd());
            build.setAudiencePasswd(meetingRoom.getAudiencePasswd());
            build.setGuestPwd(meetingRoom.getGuestPwd());
        }

        if (!publicFlag) {
            // 私有资源，自动设置已分配资源
            build.setAssignResourceStatus(MeetingAssignStateEnum.ASSIGN_STATE_ENUM.getState());
        }
        return build;
    }

    private CommonResult checkCreateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        Integer resourceId = meetingRoomContextDTO.getResourceId();
        Integer leadTime = meetingRoomContextDTO.getLeadTime();

        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);

        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);
        Date showStartTime =
            DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(meetingRoomContextDTO.getStartTime(), now),
                DateUtils.TIME_ZONE_GMT);

        DateTime lockStartTime = DateUtil.offsetMinute(showStartTime, -leadTime);
        DateTime lockEndTime = DateUtil.offsetMinute(showStartTime, meetingRoomContextDTO.getLength() + 29);

        // 判断时区是否异常
        MeetingTimeZoneConfigPO meetingTimeZoneConfigPO = meetingTimeZoneConfigDaoService.lambdaQuery()
            .eq(MeetingTimeZoneConfigPO::getTimeZoneOffset, meetingRoomContextDTO.getTimeZoneOffset())
            .orderByAsc(MeetingTimeZoneConfigPO::getId).last("limit 1").one();
        if (ObjectUtil.isNull(meetingTimeZoneConfigPO)) {
            String timeZoneOffset = meetingRoomContextDTO.getTimeZoneOffset();
            String sub = StrUtil.sub(timeZoneOffset, 0, 7) + "00";
            // GMT+09:30
            meetingRoomContextDTO.setTimeZoneOffset(sub);
            meetingTimeZoneConfigPO =
                meetingTimeZoneConfigDaoService.lambdaQuery().eq(MeetingTimeZoneConfigPO::getTimeZoneOffset, sub)
                    .orderByAsc(MeetingTimeZoneConfigPO::getId).last("limit 1").one();

            log.info("修改时区默认展示，由旧时区：{} 转化成新时区：{}", timeZoneOffset, sub);
            // return CommonResult.error(GlobalErrorCodeConstants.TIME_OFFSET_ERROR);
        }

        // 开始时间小于当前时间
        if (showStartTime.before(now)) {
            log.info("开始时间小于当前时间,开始时间：{}，当前时间：{}", showStartTime, now);
            return CommonResult.error(GlobalErrorCodeConstants.HW_START_TIME_ERROR);
        }
        // 无法创建3个月后的会议
        if (showStartTime.after(DateUtil.offsetMonth(now, 3))) {
            log.info("无法创建3个月后的会议,开始时间：{}，3个月后时间：{}", showStartTime, DateUtil.offsetMonth(now, 3));
            return CommonResult.error(GlobalErrorCodeConstants.HW_START_TIME_ERROR);
        }
        if (ObjectUtil.isNull(meetingResourcePO)) {
            // 资源不存在
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_RESOURCE);
        }
        // 超出资源过期时间
        if (DateUtil.convertTimeZone(meetingResourcePO.getExpireDate(), ZoneId.of("GMT")).before(lockEndTime)) {
            // todo 过期时间词条处理
            return CommonResult.error(GlobalErrorCodeConstants.MORE_THAN_RESOURCE_EXPIRE_ERROR,
                Collections.singletonList(DateUtil.convertTimeZone(meetingResourcePO.getExpireDate(),
                    ZoneId.of(meetingRoomContextDTO.getTimeZoneOffset()))));
        }

        FreeResourceListDTO freeResourceListDTO = wrapperFreeResourceListDTO(meetingRoomContextDTO);
        if (!getFreeResourceList(freeResourceListDTO).getData().stream().anyMatch(t -> t.getId().equals(resourceId))) {
            // 判断资源是否已被使用
            return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_USED);
        }
        // 判断私有资源，是否有权使用
        if ((meetingResourcePO.getStatus().equals(MeetingResourceStateEnum.PRIVATE.getState()) && !ObjectUtil.equals(
            meetingRoomContextDTO.getImUserId(), meetingResourcePO.getOwnerImUserId()))) {
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_USE_PERSONAL_RESOURCE_ERROR);
        }

        Long count = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getOwnerImUserId, meetingRoomContextDTO.getImUserId())
            .notLike(MeetingRoomInfoPO::getResourceType, "-")
            // 非结束的会议
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).count();
        if (!meetingResourcePO.getStatus().equals(MeetingResourceStateEnum.PRIVATE.getState()) && count >= 2) {
            // 每个用户只可同时存在2个预约的公用会议室，超出时，则主页创建入口，提示”只可以同时存在2个预约的会议室，不可再次预约“
            return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_MORE_THAN);
        }
        // 校验与会者人数
        if (ObjectUtil.isNotEmpty(
            meetingRoomContextDTO.getAttendees()) && meetingResourcePO.getSize() - 1 < meetingRoomContextDTO.getAttendees()
            .size()) {
            // 与会者人数无法超过资源限定人数
            return CommonResult.error(GlobalErrorCodeConstants.MORE_THAN_RESOURCE_SIZE_ERROR);
        }
        Tuple2<MeetingResourcePO, MeetingTimeZoneConfigPO> of = Tuples.of(meetingResourcePO, meetingTimeZoneConfigPO);
        return CommonResult.success(of);
    }

    private FreeResourceListDTO wrapperFreeResourceListDTO(MeetingRoomContextDTO meetingRoomContextDTO) {

        FreeResourceListDTO freeResourceListDTO = new FreeResourceListDTO();
        freeResourceListDTO.setImUserId(meetingRoomContextDTO.getImUserId());
        freeResourceListDTO.setLevelCode(meetingRoomContextDTO.getLevelCode());
        freeResourceListDTO.setStartTime(meetingRoomContextDTO.getStartTime());
        freeResourceListDTO.setLength(meetingRoomContextDTO.getLength());
        freeResourceListDTO.setResourceType(meetingRoomContextDTO.getResourceType());
        freeResourceListDTO.setTimeZoneOffset(meetingRoomContextDTO.getTimeZoneOffset());
        return freeResourceListDTO;
    }

    private CommonResult checkUpdateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        Integer resourceId = meetingRoomContextDTO.getResourceId();

        Integer leadTime = meetingRoomContextDTO.getLeadTime();

        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
        // 展示开始时间
        Date showStartTime =
            DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(meetingRoomContextDTO.getStartTime(), now),
                DateUtils.TIME_ZONE_GMT);
        // 展示开始时间
        DateTime showEndTime = DateUtil.offsetMinute(showStartTime, meetingRoomContextDTO.getLength());
        // 锁定开始时间
        DateTime lockStartTime = DateUtil.offsetMinute(showStartTime, -leadTime);
        // 锁定结束时间
        DateTime lockEndTime = DateUtil.offsetMinute(showStartTime, meetingRoomContextDTO.getLength() + 29);

        // 判断时区是否异常
        MeetingTimeZoneConfigPO meetingTimeZoneConfigPO = meetingTimeZoneConfigDaoService.lambdaQuery()
            .eq(MeetingTimeZoneConfigPO::getTimeZoneOffset, meetingRoomContextDTO.getTimeZoneOffset())
            .orderByAsc(MeetingTimeZoneConfigPO::getId).last("limit 1").one();
        if (ObjectUtil.isNull(meetingTimeZoneConfigPO)) {
            String timeZoneOffset = meetingRoomContextDTO.getTimeZoneOffset();
            String sub = StrUtil.sub(timeZoneOffset, 0, 7) + "00";
            // GMT+09:30
            meetingRoomContextDTO.setTimeZoneOffset(sub);
            meetingTimeZoneConfigPO =
                meetingTimeZoneConfigDaoService.lambdaQuery().eq(MeetingTimeZoneConfigPO::getTimeZoneOffset, sub)
                    .orderByAsc(MeetingTimeZoneConfigPO::getId).last("limit 1").one();
            log.info("修改时区默认展示，由旧时区：{} 转化成新时区：{}", timeZoneOffset, sub);
            // return CommonResult.error(GlobalErrorCodeConstants.TIME_OFFSET_ERROR);
        }
        if (ObjectUtil.isNull(meetingResourcePO)) {
            // 资源不存在
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_RESOURCE);
        }
        if (ObjectUtil.isNotNull(showStartTime)) {
            // 开始时间小于当前时间
            if (showStartTime.before(now)) {
                return CommonResult.error(GlobalErrorCodeConstants.HW_START_TIME_ERROR);
            }
            // 无法创建3个月后的会议
            if (showStartTime.after(DateUtil.offsetMonth(now, 3))) {
                return CommonResult.error(GlobalErrorCodeConstants.HW_START_TIME_ERROR);
            }
            if (DateUtil.convertTimeZone(meetingResourcePO.getExpireDate(), ZoneId.of("GMT")).before(lockEndTime)) {
                // todo 过期时间词条处理
                return CommonResult.error(GlobalErrorCodeConstants.MORE_THAN_RESOURCE_EXPIRE_ERROR,
                    Collections.singletonList(DateUtil.convertTimeZone(meetingResourcePO.getExpireDate(),
                        ZoneId.of(meetingRoomContextDTO.getTimeZoneOffset()))));
            }
        }
        MeetingRoomInfoPO byId = meetingRoomInfoDaoService.getById(meetingRoomContextDTO.getMeetingRoomId());
        if (ObjectUtil.isNull(byId)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_ROOM_INFO);
        }
        // 开始后无法修改
        String state = byId.getState();
        if (!MeetingRoomStateEnum.Schedule.getState().equals(state)) {
            // 默认-'Schedule',会议状态非预约
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_MOD_MEETING_ROOM);
        }

        Integer oldResourceId = byId.getResourceId();

        // 判断新资源是否已被使用
        if (!oldResourceId.equals(resourceId) || !byId.getShowStartTime()
            .equals(showStartTime) || !byId.getLockEndTime().equals(showEndTime)) {

            List<MeetingRoomInfoPO> occupiedMeetingRoom =
                getOccupiedMeetingRoom(Collections.singletonList(resourceId), lockStartTime, lockEndTime);
            if (occupiedMeetingRoom.stream().filter(t -> !t.getId().equals(meetingRoomContextDTO.getMeetingRoomId()))
                .count() > 0) {
                // 资源存在被占用的相关会议
                return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_USED);
            }

        }
        // 判断私有资源，是否有权使用
        if ((meetingResourcePO.getStatus().equals(MeetingResourceStateEnum.PRIVATE.getState()) && !ObjectUtil.equals(
            meetingRoomContextDTO.getImUserId(), meetingResourcePO.getOwnerImUserId()))) {
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_USE_PERSONAL_RESOURCE_ERROR);
        }
        // 校验与会者人数
        if (ObjectUtil.isNotEmpty(
            meetingRoomContextDTO.getAttendees()) && meetingResourcePO.getSize() - 1 < meetingRoomContextDTO.getAttendees()
            .size()) {
            // 与会者人数无法超过资源限定人数
            return CommonResult.error(GlobalErrorCodeConstants.MORE_THAN_RESOURCE_SIZE_ERROR);
        }

        // 校验操作权限
        String imUserId = meetingRoomContextDTO.getImUserId();
        String ownerImUserId = byId.getOwnerImUserId();
        if (!ownerImUserId.equals(imUserId)) {
            // 非主持人本人，无法编辑
            return CommonResult.error(GlobalErrorCodeConstants.OPERATE_AUTH_ERROR);
        }

        Tuple3<MeetingRoomInfoPO, MeetingResourcePO, MeetingTimeZoneConfigPO> of =
            Tuples.of(byId, meetingResourcePO, meetingTimeZoneConfigPO);
        return CommonResult.success(of);
    }

    /**
     * 编辑会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult updateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        log.info("【编辑会议】开始，参数为：{}", meetingRoomContextDTO);
        if (ObjectUtil.isEmpty(meetingRoomContextDTO.getTimeZoneOffset())) {
            meetingRoomContextDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
            if (ObjectUtil.isNotEmpty(meetingRoomContextDTO.getStartTime())) {
                // 时区为空,则为东8
                meetingRoomContextDTO.setStartTime(DateUtil.offsetHour(meetingRoomContextDTO.getStartTime(), -8));
            }
        }

        Integer resourceId = meetingRoomContextDTO.getResourceId();
        RLock lock = redissonClient.getLock(CacheKeyUtil.getResourceLockKey(resourceId));

        try {
            if (lock.isLocked()) {
                // 资源锁定中
                log.error("【编辑会议】 当前资源");
                return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_OPERATED_ERROR);
            }
            // 资源维度锁定
            lock.lock(10, TimeUnit.SECONDS);
            CommonResult checkResult = checkUpdateMeetingRoom(meetingRoomContextDTO);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            Boolean publicFlag = NumberUtil.isNumber(meetingRoomContextDTO.getResourceType());
            Tuple3<MeetingRoomInfoPO, MeetingResourcePO, MeetingTimeZoneConfigPO> tuple3 =
                (Tuple3<MeetingRoomInfoPO, MeetingResourcePO, MeetingTimeZoneConfigPO>)checkResult.getData();
            MeetingRoomInfoPO oldMeetingRoomInfoPO = tuple3.getT1();
            MeetingResourcePO meetingResourcePO = tuple3.getT2();
            MeetingTimeZoneConfigPO meetingTimeZoneConfigPO = tuple3.getT3();

            meetingRoomContextDTO.setVmrId(meetingResourcePO.getVmrId());
            meetingRoomContextDTO.setVmrMode(meetingResourcePO.getVmrMode());
            meetingRoomContextDTO.setMeetingCode(oldMeetingRoomInfoPO.getHwMeetingCode());
            meetingRoomContextDTO.setConferenceId(oldMeetingRoomInfoPO.getConferenceId());
            meetingRoomContextDTO.setTimeZoneID(meetingTimeZoneConfigPO.getTimeZoneId());
            // 编辑会议
            Integer vmrMode = meetingResourcePO.getVmrMode();
            // 查询是否该资源已分配，
            String currentUseImUserId = meetingResourcePO.getCurrentUseImUserId();
            meetingRoomContextDTO.setCurrentResourceUserId(currentUseImUserId);
            /*   if (publicFlag && StringUtils.isNotBlank(currentUseImUserId)) {
                //如果已分配，则执行 回收-分配-再回收
                log.info("【编辑会议】回收达成条件是1 currentUseImUserId:{}", currentUseImUserId);
                hwMeetingCommonService.disassociateVmr(currentUseImUserId,
                    Collections.singletonList(meetingResourcePO.getVmrId()));
            }*/
            MeetingRoomModel meetingRoom = new MeetingRoomModel();
            meetingRoom.setHwMeetingId(oldMeetingRoomInfoPO.getHwMeetingId());
            meetingRoom.setHwMeetingCode(oldMeetingRoomInfoPO.getHwMeetingCode());
            meetingRoom.setState("");
            meetingRoom.setChairmanPwd(oldMeetingRoomInfoPO.getHostPwd());
            meetingRoom.setConferenceId(oldMeetingRoomInfoPO.getConferenceId());
            // meetingRoom.setGuestPwd();
            // meetingRoom.setAudiencePasswd();
            // meetingRoom.setGeneralPwd();

            if (hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                .existMeetingRoom(oldMeetingRoomInfoPO.getHwMeetingCode())) {
                // 存在会议，则编辑
                // 1、修改华为云会议
                hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                    .updateMeetingRoom(meetingRoomContextDTO);
            } else {
                // 华为云不存在，为新增
                meetingRoom = hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                    .createMeetingRoom(meetingRoomContextDTO);

            }

            // 包装po实体
            MeetingRoomInfoPO meetingRoomInfoPO =
                packMeetingRoomInfoPO(meetingRoomContextDTO, meetingRoom, meetingResourcePO);
            // 2、修改本地会议
            meetingRoomInfoDaoService.updateById(meetingRoomInfoPO);
            // 3、锁定资源，更改资源状态为共有预约
            publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_UP);
            // 4、更换资源
            if (!oldMeetingRoomInfoPO.getResourceId().equals(meetingRoomContextDTO.getResourceId())) {
                // 1、变更旧资源状态
                publicResourceHoldHandle(oldMeetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_DOWN);
            }
            // 5、处理与会者相关
            updateMeetingRoomAttendee(meetingRoomInfoPO, meetingRoomContextDTO.getAttendees());

            log.info("【编辑会议】成功，会议id：{}", meetingRoomContextDTO.getMeetingRoomId());
            return CommonResult.success(null);

        } catch (Exception e) {
            log.error("【编辑会议】异常", e);
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.info("【编辑会议】释放资源锁：资源id：{}", resourceId);
                lock.unlock();
            }
        }
    }

    private void updateMeetingRoomAttendee(MeetingRoomInfoPO meetingRoomInfoPO, List<MeetingAttendeeDTO> attendees) {
        if (ObjectUtil.isEmpty(attendees)) {
            return;
        }
        Long meetRoomId = meetingRoomInfoPO.getId();
        DateTime now = DateUtil.date();
        List<MeetingAttendeePO> newMeetingAttendeeList = attendees.stream().map(
                t -> MeetingAttendeePO.builder().attendeeUserId(t.getAttendeeUserId()).meetingRoomId(meetRoomId)
                    .attendeeUserName(t.getAttendeeUserName()).attendeeUserHeadUrl(t.getAttendeeUserHeadUrl())
                    .source(MeetingUserJoinSourceEnum.APPOINT.getCode()).createTime(now).updateTime(now).build()).distinct()
            .collect(Collectors.toList());

        List<MeetingAttendeePO> oldMeetingAttendeeList =
            meetingAttendeeDaoService.lambdaQuery().eq(MeetingAttendeePO::getMeetingRoomId, meetRoomId).list();

        List<MeetingAttendeePO> addMeetingAttendeeList =
            CollectionUtil.subtractToList(newMeetingAttendeeList, oldMeetingAttendeeList);
        if (CollectionUtil.isNotEmpty(addMeetingAttendeeList)) {
            // 新增的
            meetingAttendeeDaoService.saveBatch(addMeetingAttendeeList);
            //
            roomAsyncTaskService.batchSendIMMessage(meetingRoomInfoPO,
                addMeetingAttendeeList.stream().map(MeetingAttendeePO::getAttendeeUserId).collect(Collectors.toList()));

        }
        List<MeetingAttendeePO> delMeetingAttendeeList =
            CollectionUtil.subtractToList(oldMeetingAttendeeList, newMeetingAttendeeList);
        if (CollectionUtil.isNotEmpty(delMeetingAttendeeList)) {
            // 删除的
            List<Long> delIds =
                delMeetingAttendeeList.stream().map(MeetingAttendeePO::getId).collect(Collectors.toList());
            meetingAttendeeDaoService.removeBatchByIds(delIds);
        }

    }

    /**
     * 查询会议详情
     *
     * @param meetingRoomId
     * @return
     */
    @Override
    public CommonResult<MeetingRoomDetailDTO> getMeetingRoom(Long meetingRoomId, String imUserId) {
        log.info("【查询会议详情】入参，meetingRoomId为：{}", meetingRoomId);
        MeetingRoomInfoPO meetingRoomInfoPO = meetingRoomInfoDaoService.getById(meetingRoomId);
        if (ObjectUtil.isNull(meetingRoomInfoPO)) {
            return CommonResult.success(null);
        }
        List<MeetingAttendeePO> list =
            meetingAttendeeDaoService.lambdaQuery().eq(MeetingAttendeePO::getMeetingRoomId, meetingRoomId).list();

        Integer vmrMode = meetingRoomInfoPO.getVmrMode();
        MeetingRoomDetailDTO result = packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, list);
        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode)).setMeetingRoomDetail(result);
        return CommonResult.success(result);
    }

    private MeetingRoomDetailDTO packBaseMeetingRoomDetailDTO(MeetingRoomInfoPO meetingRoomInfoPO,
        List<MeetingAttendeePO> list) {
        MeetingRoomDetailDTO result = BeanUtil.copyProperties(meetingRoomInfoPO, MeetingRoomDetailDTO.class);
        // 设置会议时长
        Date relStartTime = meetingRoomInfoPO.getRelStartTime();
        Date relEndTime = meetingRoomInfoPO.getRelEndTime();
        if (ObjectUtil.isAllNotEmpty(relStartTime, relEndTime)) {
            Duration duration = Duration.between(LocalDateTimeUtil.of(relStartTime), LocalDateTimeUtil.of(relEndTime));
            long hours = duration.toHours();
            long minutes = duration.minusHours(hours).toMinutes();
            long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

            // 输出结果
            result.setMeetingLength(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }

        String resourceType = result.getResourceType();
        if (NumberUtil.isNumber(resourceType)) {
            // 共有会议类型
            result.setResourceTypeWordKey(MeetingResourceEnum.getByCode(Integer.parseInt(resourceType)).getWordKey());
        } else {
            // 私有会议
            result.setResourceTypeWordKey(MeetingResourceEnum.specialResourceKey);
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(result.getResourceTypeDesc());
            if (matcher.find()) {
                result.setResourceTypeWordValue(Integer.parseInt(matcher.group()));
            }
        }

        if (ObjectUtil.isNotEmpty(list)) {
            List<MeetingAttendeeVO> collect = list.stream().map(t -> {
                MeetingAttendeeVO meetingAttendeeVO = new MeetingAttendeeVO();
                meetingAttendeeVO.setMeetingRoomId(meetingRoomInfoPO.getId());
                meetingAttendeeVO.setAttendeeUserId(t.getAttendeeUserId());
                meetingAttendeeVO.setAttendeeUserName(t.getAttendeeUserName());
                meetingAttendeeVO.setSource(t.getSource());
                meetingAttendeeVO.setAttendeeUserHeadUrl(t.getAttendeeUserHeadUrl());
                return meetingAttendeeVO;
            }).collect(Collectors.toList());
            result.setAttendees(collect);

        }

        return result;
    }

    public static void main(String[] args) {

        String timeZoneOffset = "GMT+10:20";
        String sub = StrUtil.sub(timeZoneOffset, 0, 7);
        // GMT+09:30
        String a = sub + "00";

        System.out.println(a);
        // Object[] params = new Object[] {"1234"};
        // String msg = MessageFormat.format("验证码:{0},您正在登录管理后台，1分钟内输入有效。", params);
        // System.out.println(msg);

        Date date1 = DateUtil.parse("2022-01-01 00:00:00");
        Date date2 = DateUtil.parse("2022-01-01 14:30:00");
        Duration duration = Duration.between(LocalDateTimeUtil.of(date1), LocalDateTimeUtil.of(date2));
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

        // 输出结果
        // System.out.printf("%02d:%02d:%02d", hours, minutes, seconds);

        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
        DateTime date = DateUtil.date((Date)null);
        System.out.println(date);
        Date showStartTime1 = DateUtils.roundToHalfHour(now, DateUtils.TIME_ZONE_GMT);

        System.out.println(showStartTime1);

    }

    /**
     * 取消会议
     *
     * @param cancelMeetingRoomDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult cancelMeetingRoom(CancelMeetingRoomDTO cancelMeetingRoomDTO) {
        log.info("【取消会议】参数为：{}", cancelMeetingRoomDTO);
        Long meetingRoomId = cancelMeetingRoomDTO.getMeetingRoomId();
        MeetingRoomInfoPO byId = meetingRoomInfoDaoService.getById(meetingRoomId);
        if (ObjectUtil.isNull(byId)) {
            return CommonResult.success(null);
        }
        String state = byId.getState();
        if (!MeetingRoomStateEnum.Schedule.getState().equals(state)) {
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_CANCEL_MEETING_ROOM);
        }
        String imUserId = cancelMeetingRoomDTO.getImUserId();
        if (ObjectUtil.isNotEmpty(imUserId) && !byId.getOwnerImUserId().equals(imUserId)) {
            return CommonResult.error(GlobalErrorCodeConstants.OPERATE_AUTH_ERROR);
        }

        Integer resourceId = byId.getResourceId();
        MeetingResourcePO byId1 = meetingResourceDaoService.getById(resourceId);
        Integer vmrMode = byId.getVmrMode();
        String ownerImUserId = byId.getOwnerImUserId();
        String hwMeetingCode = byId.getHwMeetingCode();
        String vmrId = byId1.getVmrId();
        String currentUseImUserId = byId1.getCurrentUseImUserId();
        // meetingRoomInfoDaoService.removeById(meetingRoomId);
        meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getId, meetingRoomId)
            .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Cancel.getState())
            .set(MeetingRoomInfoPO::getIsDeleted, DeleteEnum.DELETE.getStatus()).update();
        // 直接取消华为云会议
        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode)).cancelMeetingRoom(
            new CancelMeetingRoomModel(ownerImUserId, hwMeetingCode, vmrId, NumberUtil.isNumber(byId.getResourceType()),
                currentUseImUserId, byId1.getId()));
        // 释放资源
        publicResourceHoldHandle(resourceId, MeetingResourceHandleEnum.HOLD_DOWN);
        // 会议资源已分配，则取消资源占用
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
        Date lockStartTime = byId.getLockStartTime();
        Date lockEndTime = byId.getLockEndTime();
        Integer status = byId1.getStatus();
        Integer assignResourceStatus = byId.getAssignResourceStatus();
        if (!MeetingResourceStateEnum.PRIVATE.getState().equals(status) && now.isAfterOrEquals(
            lockStartTime) && now.isBeforeOrEquals(lockEndTime)) {
            hwMeetingCommonService.disassociateVmr(byId.getOwnerImUserId(),
                Collections.singletonList(byId1.getVmrId()));
        }
        return CommonResult.success(null);
    }

    /**
     * 挂起资源 释放资源 返回是否操作成功
     *
     * @param resourceId
     * @param meetingResourceHandleEnum
     */
    public Boolean publicResourceHoldHandle(Integer resourceId, MeetingResourceHandleEnum meetingResourceHandleEnum) {
        log.info("【资源挂起释放】当前资源：{} ，进行挂起释放操作：{}", resourceId, meetingResourceHandleEnum);
        RLock lock = redissonClient.getLock(CacheKeyUtil.getResourceLockKey(resourceId));
        try {

            if (lock.isLocked() && !lock.isHeldByCurrentThread()) {
                // 资源锁定中,无法操作
                log.error("【资源挂起释放】资源锁定中，无法进行操作，将异常返回，资源id:{},操作类型：{}", resourceId,
                    meetingResourceHandleEnum);
                throw new ServiceException(GlobalErrorCodeConstants.RESOURCE_OPERATED_ERROR);
            }
            // 资源维度锁定
            lock.lock(10, TimeUnit.SECONDS);

            MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
            Integer status = meetingResourcePO.getStatus();
            if (MeetingResourceStateEnum.PRIVATE.getState().equals(status)) {
                log.info("【资源挂起释放】当前资源：{} 为私有资源，无需进行挂起释放操作", resourceId);
                return Boolean.FALSE;
            }

            boolean subscribeFlag = MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState().equals(status);
            switch (meetingResourceHandleEnum) {
                case HOLD_UP:
                    if (MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState().equals(status)) {
                        // 已经是公有预约
                        log.info("【资源挂起释放】当前资源已经处理公有预约状态，无需挂起,资源id：{}", resourceId);
                        return Boolean.FALSE;
                    }
                    if (MeetingResourceStateEnum.REDISTRIBUTION.getState().equals(status)) {
                        // 当前状态是预分配，无法挂起
                        log.info("【资源挂起释放】当前资源是预分配状态，无法挂起,资源id：{}", resourceId);
                        throw new ServiceException(GlobalErrorCodeConstants.RESOURCE_REDISTRIBUTION_OPERATED_ERROR);
                    }
                    // 当前状态为公有空闲，可以置为公有预约
                    boolean update = meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceId)
                        .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PUBLIC_FREE.getState())
                        .set(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState())
                        .update();
                    log.info("【资源挂起释放】修改资源状态为公有预约状态结果：{},资源id：{}", update, resourceId);
                    return Boolean.TRUE;
                case HOLD_DOWN:
                    // 要释放的情况，状态是共有预约或者共有预分配状态
                    // 当前资源已经是公有空闲，无需释放
                    if (MeetingResourceStateEnum.PUBLIC_FREE.getState().equals(status)) {
                        log.info("【资源挂起释放】当前资源已经处理公有空闲状态，无需释放,资源id：{}", resourceId);
                        return Boolean.FALSE;
                    }

                    // 查询是否有会议室占用该资源，如果没有则修改状态置为共有空闲或者私有
                    Long count =
                        meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).count();
                    if (count == 0) {
                        // 当前无占用会议室
                        boolean update1 =
                            meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceId)
                                // 当前状态为共有预约。释放资源后，如果
                                .set(MeetingResourcePO::getStatus,
                                    subscribeFlag ? MeetingResourceStateEnum.PUBLIC_FREE.getState()
                                        : MeetingResourceStateEnum.PRIVATE.getState())
                                .set(!subscribeFlag, MeetingResourcePO::getCurrentUseImUserId,
                                    meetingResourcePO.getOwnerImUserId()).update();
                        // 如果私有，则分配资源
                        if (MeetingResourceStateEnum.REDISTRIBUTION.getState().equals(status)) {
                            log.info("【资源挂起释放】将预分配资源分配给私人，resourceId:{},ownerId：{}",
                                meetingResourcePO.getId(), meetingResourcePO.getOwnerImUserId());
                            hwMeetingCommonService.associateVmr(meetingResourcePO.getOwnerImUserId(),
                                Collections.singletonList(meetingResourcePO.getVmrId()));
                        }

                        log.info("【资源挂起释放】修改资源状态为公有空闲状态结果：{}", update1);
                        return Boolean.TRUE;
                    } else {
                        log.info(
                            "【资源挂起释放】修改资源状态为空闲/私有状态失败无法释放,resourceId：{},当前存在相关会议数,count：{}",
                            resourceId, count);
                        return Boolean.FALSE;
                    }
                default:
                    return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error("【资源挂起释放】执行异常，资源id:{},操作类型：{},异常信息：{}", resourceId, meetingResourceHandleEnum,
                e);
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.info("【资源挂起释放】释放资源锁：资源id：{}", resourceId);
                lock.unlock();
            }
        }
    }

    /**
     * 首页查询即将召开和进行中的会议列表
     *
     * @return
     */
    @Override
    public CommonResult<FutureAndRunningMeetingRoomListVO> getFutureAndRunningMeetingRoomList(
        FutureAndRunningMeetingRoomListGetDTO futureAndRunningMeetingRoomListGetDTO) {
        String imUserId = futureAndRunningMeetingRoomListGetDTO.getFinalUserId();
        String timeZoneOffset = futureAndRunningMeetingRoomListGetDTO.getTimeZoneOffset();

        // 列表中显示最近要开始的会议，按照会议开始时间正序。最多显示30天数据
        // 已结束的会议不显示。
        // 我创建的 和 我参加的。
        List<Long> joinMeetingRoomIds =
            meetingAttendeeDaoService.lambdaQuery().select(MeetingAttendeePO::getMeetingRoomId)
                .eq(MeetingAttendeePO::getAttendeeUserId, imUserId).list().stream()
                .map(MeetingAttendeePO::getMeetingRoomId).collect(Collectors.toList());

        Consumer<LambdaQueryWrapper<MeetingRoomInfoPO>> consumer =
            wrapper -> wrapper.or(ObjectUtil.isNotEmpty(joinMeetingRoomIds),
                    wrapper1 -> wrapper1.in(MeetingRoomInfoPO::getId, joinMeetingRoomIds))
                .or(wrapper2 -> wrapper2.eq(MeetingRoomInfoPO::getOwnerImUserId, imUserId));
        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .in(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState(),
                MeetingRoomStateEnum.Created.getState()).orderByDesc(MeetingRoomInfoPO::getLockStartTime)
            .nested(consumer).last(" limit 30").list();
        //
        FutureAndRunningMeetingRoomListVO futureAndRunningMeetingRoomListVO =
            packFutureAndRunningMeetingRoomListVO(list, timeZoneOffset);
        // hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.CLOUD.getVmrMode()).queryMeetingRoomList(imUserId);

        return CommonResult.success(futureAndRunningMeetingRoomListVO);
    }

    private FutureAndRunningMeetingRoomListVO packFutureAndRunningMeetingRoomListVO(List<MeetingRoomInfoPO> list,
        String timeZoneOffset) {
        FutureAndRunningMeetingRoomListVO futureAndRunningMeetingRoomListVO = new FutureAndRunningMeetingRoomListVO();
        List<MeetingRoomDetailDTO> meetingRoomDetailDTOS = BeanUtil.copyToList(list, MeetingRoomDetailDTO.class);
        TreeMap<String, List<MeetingRoomDetailDTO>> sortMap = new TreeMap<>(Comparator.comparing(DateUtil::parse));
        Map<String, List<MeetingRoomDetailDTO>> collect = meetingRoomDetailDTOS.stream()
            // peek(t -> t.setShowStartTime(
            // DateUtils.convertTimeZone(t.getShowStartTime(), DateUtils.TIME_ZONE_GMT, ZoneId.of(timeZoneOffset))))
            .collect(Collectors.groupingBy(f -> DateUtil.format(
                DateUtils.convertTimeZone(f.getShowStartTime(), DateUtils.TIME_ZONE_GMT, ZoneId.of(timeZoneOffset)),
                DatePattern.NORM_DATE_PATTERN), () -> sortMap, Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<MeetingRoomDetailDTO>(Comparator.comparing(MeetingRoomDetailDTO::getShowStartTime)
                    .thenComparing(MeetingRoomDetailDTO::getHwMeetingCode))), Lists::newArrayList)));
        futureAndRunningMeetingRoomListVO.setRooms(collect);
        return futureAndRunningMeetingRoomListVO;
    }

    /**
     * @return
     */
    @Override
    public CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList(
        HistoryMeetingRoomListGetDTO historyMeetingRoomListGetDTO) {
        Integer month = historyMeetingRoomListGetDTO.getMonth();
        String imUserId = historyMeetingRoomListGetDTO.getFinalUserId();
        String timeZoneOffset = historyMeetingRoomListGetDTO.getTimeZoneOffset();

        DateTime dateTime = getMonth(month);
        DateTime start = DateUtil.beginOfMonth(dateTime);
        DateTime end = DateUtil.endOfMonth(dateTime);
        List<Long> joinMeetingRoomIds =
            meetingAttendeeDaoService.lambdaQuery().select(MeetingAttendeePO::getMeetingRoomId)
                .eq(MeetingAttendeePO::getAttendeeUserId, imUserId).list().stream()
                .map(MeetingAttendeePO::getMeetingRoomId).collect(Collectors.toList());
        Consumer<LambdaQueryWrapper<MeetingRoomInfoPO>> consumer =
            wrapper -> wrapper.or(ObjectUtil.isNotEmpty(joinMeetingRoomIds),
                    wrapper1 -> wrapper1.in(MeetingRoomInfoPO::getId, joinMeetingRoomIds))
                .or(wrapper2 -> wrapper2.eq(MeetingRoomInfoPO::getOwnerImUserId, imUserId));

        List<MeetingRoomInfoPO> list =
            meetingRoomInfoDaoService.lambdaQuery().ge(MeetingRoomInfoPO::getLockStartTime, start)
                .le(MeetingRoomInfoPO::getLockEndTime, end)
                .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).nested(consumer)
                .orderByDesc(MeetingRoomInfoPO::getCreateTime).list();
        List<Long> meetingRoomIdList = list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList());
        Map<Long, List<MeetingAttendeePO>> roomIdAttendeeMap = Maps.newHashMap();
        if (ObjectUtil.isNotEmpty(meetingRoomIdList)) {
            roomIdAttendeeMap =
                meetingAttendeeDaoService.lambdaQuery().in(MeetingAttendeePO::getMeetingRoomId, meetingRoomIdList)
                    .list().stream().collect(Collectors.groupingBy(MeetingAttendeePO::getMeetingRoomId));
        }

        Map<Long, List<MeetingAttendeePO>> finalRoomIdAttendeeMap = roomIdAttendeeMap;
        List<MeetingRoomDetailDTO> collect =
            list.stream().map(t -> packBaseMeetingRoomDetailDTO(t, finalRoomIdAttendeeMap.get(t.getId())))
                .collect(Collectors.toList());
        return CommonResult.success(collect);
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
        Date date = availableResourcePeriodGetDTO.getDate();
        if (ObjectUtil.isEmpty(availableResourcePeriodGetDTO.getTimeZoneOffset())) {
            availableResourcePeriodGetDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
        }

        ZoneId userZoneId = ZoneId.of(availableResourcePeriodGetDTO.getTimeZoneOffset());
        // 用户当前时间
        DateTime now = DateUtils.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT, userZoneId);

        // 查询不到当前时间之前的会议
        if (date.before(DateUtil.beginOfDay(now))) {
            return CommonResult.success(Collections.emptyList());
        }
        Integer resourceId = availableResourcePeriodGetDTO.getResourceId();
        MeetingResourcePO byId = meetingResourceDaoService.getById(resourceId);

        if (ObjectUtil.isEmpty(byId)) {
            // 资源不存在
            return CommonResult.success(null);
        }

        if (date.after(DateUtil.convertTimeZone(byId.getExpireDate(), userZoneId))) {
            // 已过期
            return CommonResult.success(Collections.emptyList());
        }

        DateTime beginOfDay = DateUtil.beginOfDay(date);
        // 重新计算UTC开始时间
        beginOfDay = DateUtils.convertTimeZone(beginOfDay, userZoneId, DateUtils.TIME_ZONE_GMT);
        DateTime endOfDay = DateUtil.endOfDay(date);
        endOfDay = DateUtils.convertTimeZone(endOfDay, userZoneId, DateUtils.TIME_ZONE_GMT);
        // 查询与当日有交叉的会议
        List<MeetingRoomInfoPO> occupiedMeetingRoom =
            getOccupiedMeetingRoom(Collections.singletonList(resourceId), beginOfDay, endOfDay);
        ArrayList<FreeTimeCalculatorUtil.TimeRange> timeRanges = Lists.newArrayList();
        // 如果存在有交叉上一天或者下一天的会议，则掐头去尾，重设置临界值时间
        for (MeetingRoomInfoPO meetingRoomInfoPO : occupiedMeetingRoom) {
            // UTC开始时间
            DateTime lockStartTime = DateUtil.date(meetingRoomInfoPO.getLockStartTime());
            // UTC结束时间
            DateTime lockEndTime = DateUtil.date(meetingRoomInfoPO.getLockEndTime());
            // 设置开始时间
            if (lockStartTime.before(beginOfDay)) {
                lockStartTime = beginOfDay;
            }

            /*   else {
                lockStartTime = DateUtil.offsetMinute(lockStartTime, -30);
            }*/

            // 设置结束时间
            if (lockEndTime.after(endOfDay)) {
                lockEndTime = endOfDay;
            }
            /* else {
                lockEndTime = DateUtil.offsetMinute(lockEndTime, 30);
            }*/
            timeRanges.add(new FreeTimeCalculatorUtil.TimeRange(lockStartTime, lockEndTime, userZoneId));
        }

        // 查询该资源当日占用情况
        /* List<FreeTimeCalculatorUtil.TimeRange> collect =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                .between(MeetingRoomInfoPO::getLockStartTime, DateUtil.beginOfDay(date), DateUtil.endOfDay(date))
                .between(MeetingRoomInfoPO::getLockEndTime, DateUtil.beginOfDay(date), DateUtil.endOfDay(date))
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list().stream()
                .map(t -> new FreeTimeCalculatorUtil.TimeRange(t.getLockStartTime(), t.getLockEndTime()))
                .collect(Collectors.toList());*/
        // 最大6小时切割

        List<FreeTimeCalculatorUtil.TimeRange> rangeList =
            FreeTimeCalculatorUtil.calculateFreeTimeRanges(timeRanges, 1, 6, date, byId.getExpireDate(), userZoneId,
                availableResourcePeriodGetDTO.getLeadTime());
        List<AvailableResourcePeriodVO> result =
            rangeList.stream().map(t -> new AvailableResourcePeriodVO(t.getStart().toString(), t.getEnd().toString()))
                .collect(Collectors.toList());
        return CommonResult.success(result);
    }

    /**
     * 更新华为云会议室状态 正常普通会议，一个会结束时结束和关闭事件都会推。 周期会议，或者开启会议结束保留预约记录开关的时候只会触发会议结束，不会触发会议关闭事件
     *
     * @param hwEventReq
     * @return
     */
    @Override
    @Transactional
    public CommonResult<String> updateMeetingRoomStatus(HwEventReq hwEventReq) {
        log.info("【企业级华为云事件】推送入参：{}", hwEventReq);

        String nonce = hwEventReq.getNonce();
        EventInfo eventInfo = hwEventReq.getEventInfo();
        // 事件名
        String event = eventInfo.getEvent();
        if ("meeting.verify".equals(event)) {
            // 验证事件
            String s = JSONUtil.createObj().set("event", "meeting.verify").set("nonce", nonce).toStringPretty();
            return CommonResult.success(s);
        }
        roomAsyncTaskService.saveHwEventLog(hwEventReq);

        listeningExecutorService.submit(() -> {
            Long timestamp = eventInfo.getTimestamp();
            Payload payload = eventInfo.getPayload();
            String meetingID = payload.getMeetingInfo().getMeetingID();
            Optional<MeetingRoomInfoPO> meetingRoomInfoPOOptional =
                meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getConferenceId, meetingID)
//                    .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
                    .orderByDesc(MeetingRoomInfoPO::getCreateTime)
                    .last(" limit 1")
                    .oneOpt();
            RLongAdder count = redissonClient.getLongAdder(CacheKeyUtil.getHwMeetingRoomMaxSyncKey(meetingID));
            int maxErrorCount = 20;
            if (!meetingRoomInfoPOOptional.isPresent()) {
                log.error("事件回调数据异常，数据不存在 meetingID：{}", meetingID);
                if (count.sum() >= maxErrorCount) {
                    log.error("【企业级华为云事件】事件回调数据异常达到次数上限：{}次,会议号：{}", maxErrorCount,
                        meetingID);
                    return;
                    // return CommonResult.errorMsg("事件达到次数上限");
                }
                count.increment();
                // 5、5秒后重试，优化立即会议
                WheelTimerContext.getInstance().createTimeoutTask(timeout -> {
                    // 重试
                    hwEventReq.setRetryFlag(true);
                    updateMeetingRoomStatus(hwEventReq);
                }, 2, TimeUnit.SECONDS);
                return;
                // return CommonResult.success("");
            }

            MeetingRoomInfoPO meetingRoomInfoPO = meetingRoomInfoPOOptional.get();
            Long meetingId = meetingRoomInfoPO.getId();

            MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());

            if ("meeting.started".equals(event)) {
                // 推送会议开始事件
                boolean update = meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getId, meetingId)
                    .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState())
                    .set(MeetingRoomInfoPO::getHwMeetingId, payload.getMeetingInfo().getMeetingUUID())
                    .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Created.getState())
                    .set(MeetingRoomInfoPO::getRelStartTime, DateUtil.date(timestamp)).update();
                log.info("【企业级华为云事件】华为云会议事件开始会议id：{}，结果：{}", meetingID, update);

            } else if ("meeting.end".equals(event) || "meeting.conclude".equals(event)) {
                RLock lock = redissonClient.getLock(CacheKeyUtil.getMeetingStopLockKey(meetingID));
                if (!lock.isLocked()) {
                    try {
                        // 资源维度锁定
                        lock.lock(10, TimeUnit.SECONDS);
                        // 会议结束是一场会开会结束时触发，会议关闭是预约记录删除的时候触发
                        // 会议结束事件-当企业下的某个会议结束，服务端会推送会议结束事件消息的post请求到企业开发者回调URL。会议结束后，如果会议预定的结束时间还没到，可以再次加入该会议。
                        // 会议关闭事件
                        boolean update =
                            meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getId, meetingId)

                                .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
                                .set(MeetingRoomInfoPO::getRelEndTime, DateUtil.date(timestamp)).update();
                        // 回收资源
                        Boolean operateResult = publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(),
                            MeetingResourceHandleEnum.HOLD_DOWN);
                        if (!meetingResourcePO.getStatus().equals(MeetingResourceStateEnum.PRIVATE.getState())) {
                            hwMeetingCommonService.disassociateVmr(meetingRoomInfoPO.getOwnerImUserId(),
                                Collections.singletonList(meetingResourcePO.getVmrId()));
                        }
                        // 发放多人会议奖励
                        roomAsyncTaskService.doSendMultiPersonsAward(meetingRoomInfoPO);

                        log.info("【企业级华为云事件】华为云会议结束修改会议id：{}，结果：{}", meetingID, update);
                    } finally {
                        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                            log.info("【企业级华为云事件】释放会议锁：会议号：{}", meetingID);
                            lock.unlock();
                        }
                    }

                } else {
                    log.info("【企业级华为云事件】会议结束触发锁占用逻辑,执行跳过操作, 会议号:{}", meetingID);
                }
            } else if ("record.finish".equals(event)) {
                // 录制结束事件-当企业下的某个会议结束，服务端会推送录制结束事件消息的post请求到企业开发者回调URL
                boolean update = meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getId, meetingId)
                    .set(MeetingRoomInfoPO::getRecordStatus, 1).update();
                log.info("【企业级华为云事件】华为云会议录制会议id：{}，结果：{}", meetingID, update);
            }
        });
        return CommonResult.success(null);
    }

    /**
     * 查询会议录制详情
     *
     * @param meetingRoomId
     * @return
     */
    @Override
    public CommonResult<List<RecordVO>> getMeetingRoomRecordList(Long meetingRoomId) {

        MeetingRoomInfoPO byId = meetingRoomInfoDaoService.getById(meetingRoomId);
        if (ObjectUtil.isNull(byId) || byId.getRecordStatus() == 0) {
            return CommonResult.success(null);
        }
        List<RecordVO> recordVOS = hwMeetingCommonService.queryRecordFiles(byId.getHwMeetingId());
        return CommonResult.success(recordVOS);
    }

    /**
     * 会议类型返回
     *
     * @param imUserId
     * @param levelCode
     * @return
     */
    @Override
    public CommonResult<List<ResourceTypeVO>> getMeetingResourceTypeList(String imUserId, Integer levelCode,
        String nationId) {
        // 获取最大会议用户等级
        Integer maxResourceType = getMaxLevel(levelCode, imUserId, nationId);
        // 根据资源等级过滤资源类型
        List<ResourceTypeVO> levelResourceTypeVOList =
            Arrays.stream(MeetingResourceEnum.values()).filter(t -> t.getCode() != 0 && t.getCode() <= maxResourceType)
                .collect(Collectors.toList()).stream().map(
                    t -> ResourceTypeVO.builder().code(String.valueOf(t.getCode())).type(1).desc(t.getDesc())
                        .size(t.getValue()).wordKey(t.getWordKey()).build()).collect(Collectors.toList());
        // 查询私池
        List<MeetingResourcePO> privateResourceList =
            meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getOwnerImUserId, imUserId)
                .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState()).list();

        List<ResourceTypeVO> collect = privateResourceList.stream().map(MeetingResourcePO::getSize).distinct().map(
            t -> ResourceTypeVO.builder().type(2)
                .code(imUserId + "-" + t + "-" + MeetingResourceEnum.getBySize(t).getCode())
                .desc(String.format(privateResourceTypeFormat, t)).size(t)
                .wordKey(MeetingResourceEnum.specialResourceKey).build()).collect(Collectors.toList());

        collect.addAll(levelResourceTypeVOList);
        return CommonResult.success(collect);
    }

    /**
     * 获取某会议类型下所有会议列表
     *
     * @param resourceCode
     * @return
     */
    @Override
    public CommonResult<List<MeetingResourceVO>> getAllMeetingResourceList(String resourceCode) {
        List<MeetingResourcePO> result;
        // 是数字
        if (NumberUtil.isNumber(resourceCode)) {
            result = meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getResourceType, resourceCode)
                .ne(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState())
                .ne(MeetingResourcePO::getStatus, MeetingResourceStateEnum.REDISTRIBUTION.getState()).list().stream()
                .filter(t -> t.getExpireDate().after(DateUtil.date())).collect(Collectors.toList());

        } else {
            String[] split = resourceCode.split("-");
            String imUserId = split[0];
            String resourceSize = split[1];
            String resourceType = split[2];
            result = meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getResourceType, resourceType)
                .eq(MeetingResourcePO::getOwnerImUserId, imUserId).eq(MeetingResourcePO::getSize, resourceSize).list()
                .stream().filter(t -> t.getExpireDate().after(DateUtil.date())).collect(Collectors.toList());

        }

        return CommonResult.success(BeanUtil.copyToList(result, MeetingResourceVO.class));
    }

    @Override
    public CommonResult<MeetingRoomDetailDTO> getMeetingRoomByCode(String meetingCode) {
        log.info("【查询会议详情】 meetingCode：{}", meetingCode);

        List<MeetingRoomInfoPO> meetingRoomInfoPOList = meetingRoomInfoDaoService.lambdaQuery().and(
                wrapper1 -> wrapper1.eq(MeetingRoomInfoPO::getHwMeetingCode, meetingCode)
                    .or(wrapper2 -> wrapper2.eq(MeetingRoomInfoPO::getConferenceId, meetingCode)))
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
            .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();

        if (ObjectUtil.isEmpty(meetingRoomInfoPOList)) {
            return CommonResult.success(null);
        }
        MeetingRoomInfoPO meetingRoomInfoPO = meetingRoomInfoPOList.get(0);

        List<MeetingAttendeePO> list =
            meetingAttendeeDaoService.lambdaQuery().eq(MeetingAttendeePO::getMeetingRoomId, meetingRoomInfoPO.getId())
                .list();

        Integer vmrMode = meetingRoomInfoPO.getVmrMode();
        MeetingRoomDetailDTO result = packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, list);
        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode)).setMeetingRoomDetail(result);
        return CommonResult.success(result);
    }

    DateTime getMonth(Integer month) {
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
        now.setMutable(false);
        DateTime sixMonthsAgo = now.offset(DateField.MONTH, -6);

        while (now.isAfterOrEquals(sixMonthsAgo)) {
            if (sixMonthsAgo.monthBaseOne() == month) {
                return sixMonthsAgo;
            }
            sixMonthsAgo = sixMonthsAgo.offset(DateField.MONTH, 1);

        }
        return now;
    }

}
