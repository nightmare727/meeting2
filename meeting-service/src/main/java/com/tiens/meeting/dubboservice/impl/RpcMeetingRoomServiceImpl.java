package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.*;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.huaweicloud.sdk.meeting.v1.utils.HmacSHA256;
import com.tiens.api.dto.*;
import com.tiens.api.dto.hwevent.EventInfo;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.dto.hwevent.Payload;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.*;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.dubboservice.core.entity.CancelMeetingRoomModel;
import com.tiens.meeting.dubboservice.core.entity.MeetingRoomModel;
import com.tiens.meeting.repository.po.*;
import com.tiens.meeting.repository.service.*;
import com.tiens.meeting.util.FreeTimeCalculatorUtil;
import common.enums.*;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
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

    private final MeetingTimeZoneConfigDaoService meetingTimeZoneConfigDaoService;

    private final Map<String, HwMeetingRoomHandler> hwMeetingRoomHandlers;

    private final HwMeetingCommonService hwMeetingCommonService;

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

    public static void main(String[] args) {
        int i = Math.toIntExact(DateUtil.currentSeconds() + 100);
        System.out.println(i);

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
        //返回主持人的id
        return CommonResult.success(meetingRoomInfoPO.getOwnerImUserId());
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
        List<MeetingResourceVO> result;
        if (NumberUtil.isNumber(freeResourceListDTO.getResourceType())) {
            //2、查询用户公池资源
            result = getPublicResourceList(freeResourceListDTO);
        } else {
            DateTime startTime = DateUtil.offsetMinute(freeResourceListDTO.getStartTime(), -30);
            DateTime endTime = DateUtil.date(freeResourceListDTO.getStartTime())
                .offset(DateField.MINUTE, freeResourceListDTO.getLength() + 30);
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
            //1、查询用户私池资源
            List<MeetingResourceVO> privateResourceList = getPrivateResourceList(freeResourceListDTO);
            //去除空闲资源中被锁定的资源
            result = privateResourceList.stream().filter(t -> !lockedResourceIdList.contains(t.getId()))
                .collect(Collectors.toList());

        }
        return CommonResult.success(result);
    }

    private List<MeetingResourceVO> getPrivateResourceList(FreeResourceListDTO freeResourceListDTO) {
        List<MeetingResourcePO> list = meetingResourceDaoService.lambdaQuery()
            .eq(MeetingResourcePO::getOwnerImUserId, freeResourceListDTO.getImUserId())
            .eq(MeetingResourcePO::getResourceType, freeResourceListDTO.getResourceType()).list();
        return BeanUtil.copyToList(list, MeetingResourceVO.class);
    }

    /**
     * 查询所有可用的资源
     *
     * @param freeResourceListDTO
     * @return
     */
    private List<MeetingResourceVO> getPublicResourceList(FreeResourceListDTO freeResourceListDTO) {
        //公池该用户等级相关空闲子资源与主持人绑定公池空闲资源 的【并集】
        //查询当前时段所有被使用的资源列表

        //根据资源类型查询所有空闲资源
        List<MeetingResourcePO> levelFreeResourceList = meetingResourceDaoService.lambdaQuery()
            .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PUBLIC_FREE.getState())
            .eq(MeetingResourcePO::getResourceType, Integer.parseInt(freeResourceListDTO.getResourceType())).list();

        return BeanUtil.copyToList(levelFreeResourceList, MeetingResourceVO.class);
    }

    /**
     * 获取最大会议用户等级
     *
     * @param levelCode
     * @param imUserId
     * @return
     */
    public Integer getMaxLevel(Integer levelCode, String imUserId) {
        //根据等级查询资源
        MeetingLevelResourceConfigPO one =
            meetingLevelResourceConfigDaoService.lambdaQuery().select(MeetingLevelResourceConfigPO::getResourceType)
                .eq(MeetingLevelResourceConfigPO::getVmUserLevel, levelCode).one();
        Integer maxResourceType = one.getResourceType();
        //查询该用户的主持人等级
        Optional<MeetingHostUserPO> meetingHostUserPOOptional =
            meetingHostUserDaoService.lambdaQuery().eq(MeetingHostUserPO::getAccId, imUserId)
                .select(MeetingHostUserPO::getResourceType).oneOpt();
        if (meetingHostUserPOOptional.isPresent()) {
            //比较和等级关联的资源类型大小
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
    public CommonResult createMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        log.info("创建、预约会议开始，参数为：{}", meetingRoomContextDTO);

        CommonResult checkResult = checkCreateMeetingRoom(meetingRoomContextDTO);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        MeetingResourcePO meetingResourcePO = (MeetingResourcePO)checkResult.getData();
        meetingRoomContextDTO.setVmrId(meetingResourcePO.getVmrId());
        meetingRoomContextDTO.setVmrMode(meetingResourcePO.getVmrMode());
        meetingRoomContextDTO.setResourceStatus(meetingResourcePO.getStatus());
        //创建会议
        Integer vmrMode = meetingResourcePO.getVmrMode();

        //1、创建华为云会议
        MeetingRoomModel meetingRoom =
            hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                .createMeetingRoom(meetingRoomContextDTO);
        //包装po实体
        MeetingRoomInfoPO meetingRoomInfoPO = packMeetingRoomInfoPO(meetingRoomContextDTO, meetingRoom);
        //2、创建本地会议
        meetingRoomInfoDaoService.save(meetingRoomInfoPO);
        //3、锁定资源，更改资源状态为共有预约
        publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_UP);

        return CommonResult.success(null);
    }

    private MeetingRoomInfoPO packMeetingRoomInfoPO(MeetingRoomContextDTO meetingRoomContextDTO,
        MeetingRoomModel meetingRoom) {

        Integer resourceId = meetingRoomContextDTO.getResourceId();
        //展示开始时间
        Date showStartTime = meetingRoomContextDTO.getStartTime();
        Integer length = meetingRoomContextDTO.getLength();
        //展示结束时间
        DateTime showEndTime = DateUtil.offsetMinute(showStartTime, length);

        //锁定开始时间
        //锁定结束时间
        DateTime lockStartTime = DateUtil.offsetMinute(showStartTime, -30);
        DateTime lockEndTime = DateUtil.date(showStartTime).offset(DateField.MINUTE, length + 30);

        //查询时区配置
        MeetingTimeZoneConfigPO meetingTimeZoneConfigPO = meetingTimeZoneConfigDaoService.lambdaQuery()
            .eq(MeetingTimeZoneConfigPO::getTimeZoneId, meetingRoomContextDTO.getTimeZoneID()).one();

        MeetingRoomInfoPO build = MeetingRoomInfoPO.builder().id(meetingRoomContextDTO.getMeetingRoomId())
            .duration(meetingRoomContextDTO.getLength()).showStartTime(showStartTime).showEndTime(showEndTime)
            .lockStartTime(lockStartTime).lockEndTime(lockEndTime).resourceId(resourceId)
            .ownerImUserId(meetingRoomContextDTO.getImUserId()).timeZoneId(meetingRoomContextDTO.getTimeZoneID())
            .timeZoneOffset(meetingTimeZoneConfigPO.getTimeZoneOffset()).vmrMode(meetingRoomContextDTO.getVmrMode())
            .ownerUserName(meetingRoomContextDTO.getImUserName()).build();
        if (ObjectUtil.isNotNull(meetingRoom)) {
            build.setHwMeetingId(meetingRoom.getHwMeetingId());
            build.setHwMeetingCode(meetingRoom.getHwMeetingCode());
        }
        return build;
    }

    private CommonResult checkCreateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        //判断用户等级，您的Vmo星球等级至少Lv3才可以使用此功能
        if (meetingRoomContextDTO.getLevelCode() <= 2) {
            return CommonResult.error(GlobalErrorCodeConstants.LEVEL_NOT_ENOUGH);
        }

        Integer resourceId = meetingRoomContextDTO.getResourceId();
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
        if (ObjectUtil.isNull(meetingResourcePO)) {
            //资源不存在
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_RESOURCE);
        }
        FreeResourceListDTO freeResourceListDTO = wrapperFreeResourceListDTO(meetingRoomContextDTO);
        if (!getFreeResourceList(freeResourceListDTO).getData().stream().anyMatch(
            t -> t.getId().equals(resourceId) || (meetingResourcePO.getStatus()
                .equals(MeetingResourceStateEnum.PRIVATE.getState()) && !ObjectUtil.equals(
                meetingRoomContextDTO.getImUserId(), meetingResourcePO.getOwnerImUserId())))) {
            //判断资源是否已被使用
            return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_USED);
        }
        Long count = meetingRoomInfoDaoService.lambdaQuery()
            .eq(MeetingRoomInfoPO::getOwnerImUserId, meetingRoomContextDTO.getImUserId())
            //非结束的会议
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).count();
        if (count > 2) {
            //每个用户只可同时存在2个预约的公用会议室，超出时，则主页创建入口，提示”只可以同时存在2个预约的会议室，不可再次预约“
            return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_MORE_THAN);
        }
        return CommonResult.success(meetingResourcePO);
    }

    private FreeResourceListDTO wrapperFreeResourceListDTO(MeetingRoomContextDTO meetingRoomContextDTO) {

        FreeResourceListDTO freeResourceListDTO = new FreeResourceListDTO();
        freeResourceListDTO.setImUserId(meetingRoomContextDTO.getImUserId());
        freeResourceListDTO.setLevelCode(meetingRoomContextDTO.getLevelCode());
        freeResourceListDTO.setStartTime(meetingRoomContextDTO.getStartTime());
        freeResourceListDTO.setLength(meetingRoomContextDTO.getLength());
        freeResourceListDTO.setResourceType(meetingRoomContextDTO.getResourceType());
        return freeResourceListDTO;
    }

    private CommonResult checkUpdateMeetingRoom(MeetingRoomContextDTO meetingRoomContextDTO) {
        //判断用户等级，您的Vmo星球等级至少Lv3才可以使用此功能
        if (meetingRoomContextDTO.getLevelCode() <= 2) {
            return CommonResult.error(GlobalErrorCodeConstants.LEVEL_NOT_ENOUGH);
        }

        MeetingRoomInfoPO byId = meetingRoomInfoDaoService.getById(meetingRoomContextDTO.getMeetingRoomId());
        if (ObjectUtil.isNull(byId)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_ROOM_INFO);
        }
        //开始后无法修改
        String state = byId.getState();
        if (!MeetingRoomStateEnum.Schedule.getState().equals(state)) {
            //默认-'Schedule',会议状态非预约
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_MOD_MEETING_ROOM);
        }

        Integer resourceId = meetingRoomContextDTO.getResourceId();
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
        if (ObjectUtil.isNull(meetingResourcePO)) {
            //资源不存在
            return CommonResult.error(GlobalErrorCodeConstants.NOT_EXIST_RESOURCE);
        }
        Integer oldResourceId = byId.getResourceId();

        FreeResourceListDTO freeResourceListDTO = wrapperFreeResourceListDTO(meetingRoomContextDTO);
        //判断新资源是否已被使用
        if (!oldResourceId.equals(resourceId) && !getFreeResourceList(freeResourceListDTO).getData().stream().anyMatch(
            t -> t.getId().equals(resourceId) || (meetingResourcePO.getStatus()
                .equals(MeetingResourceStateEnum.PRIVATE.getState()) && !ObjectUtil.equals(
                meetingRoomContextDTO.getImUserId(), meetingResourcePO.getOwnerImUserId())))) {
            return CommonResult.error(GlobalErrorCodeConstants.RESOURCE_USED);
        }
        Tuple2<MeetingRoomInfoPO, MeetingResourcePO> of = Tuples.of(byId, meetingResourcePO);
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
        log.info("编辑会议开始，参数为：{}", meetingRoomContextDTO);

        CommonResult checkResult = checkUpdateMeetingRoom(meetingRoomContextDTO);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        Tuple2<MeetingRoomInfoPO, MeetingResourcePO> tuple2 =
            (Tuple2<MeetingRoomInfoPO, MeetingResourcePO>)checkResult.getData();
        MeetingRoomInfoPO oldMeetingRoomInfoPO = tuple2.getT1();
        MeetingResourcePO meetingResourcePO = tuple2.getT2();
        meetingRoomContextDTO.setVmrId(meetingResourcePO.getVmrId());
        meetingRoomContextDTO.setVmrMode(meetingResourcePO.getVmrMode());
        meetingRoomContextDTO.setMeetingCode(oldMeetingRoomInfoPO.getHwMeetingCode());
        //编辑会议
        Integer vmrMode = meetingResourcePO.getVmrMode();
        MeetingRoomModel meetingRoom =
            new MeetingRoomModel(oldMeetingRoomInfoPO.getHwMeetingId(), oldMeetingRoomInfoPO.getHwMeetingCode(), "");
        if (hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
            .existMeetingRoom(oldMeetingRoomInfoPO.getHwMeetingCode())) {
            //存在会议，则编辑
            //1、修改华为云会议
            hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                .updateMeetingRoom(meetingRoomContextDTO);
        } else {
            //华为云不存在，为新增
            meetingRoom = hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
                .createMeetingRoom(meetingRoomContextDTO);

        }

        //包装po实体
        MeetingRoomInfoPO meetingRoomInfoPO = packMeetingRoomInfoPO(meetingRoomContextDTO, meetingRoom);
        //2、修改本地会议
        meetingRoomInfoDaoService.updateById(meetingRoomInfoPO);
        //3、锁定资源，更改资源状态为共有预约
        publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_UP);
        //4、更换资源，
        if (!oldMeetingRoomInfoPO.getResourceId().equals(meetingRoomContextDTO.getResourceId())) {
            //1、变更旧资源状态
            publicResourceHoldHandle(oldMeetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_DOWN);
        }

        return CommonResult.success(null);
    }

    /**
     * 查询会议详情
     *
     * @param meetingRoomId
     * @return
     */
    @Override
    public CommonResult<MeetingRoomDetailDTO> getMeetingRoom(Long meetingRoomId, String imUserId) {
        log.info("查询会议详情，meetingRoomId为：{}", meetingRoomId);
        MeetingRoomInfoPO meetingRoomInfoPO = meetingRoomInfoDaoService.getById(meetingRoomId);
        if (ObjectUtil.isNull(meetingRoomInfoPO)) {
            return CommonResult.success(null);
        }
        Integer vmrMode = meetingRoomInfoPO.getVmrMode();
        MeetingRoomDetailDTO result = packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, true);
        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode)).setMeetingRoomDetail(result);
        return CommonResult.success(result);
    }

    private MeetingRoomDetailDTO packBaseMeetingRoomDetailDTO(MeetingRoomInfoPO meetingRoomInfoPO,
        boolean needResource) {
        MeetingRoomDetailDTO result = BeanUtil.copyProperties(meetingRoomInfoPO, MeetingRoomDetailDTO.class);
        if (needResource) {
            MeetingResourcePO byId = meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());
            result.setResourceType(byId.getResourceType());
            result.setResourceName(byId.getVmrName());
            result.setResourceType(byId.getResourceType());
        }
        return result;
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
        log.info("取消会议，参数为：{}", cancelMeetingRoomDTO);
        Long meetingRoomId = cancelMeetingRoomDTO.getMeetingRoomId();
        MeetingRoomInfoPO byId = meetingRoomInfoDaoService.getById(meetingRoomId);
        if (ObjectUtil.isNull(byId)) {
            return CommonResult.success(null);
        }
        String state = byId.getState();
        if (MeetingRoomStateEnum.Destroyed.getState().equals(state)) {
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_CANCEL_MEETING_ROOM);
        }
        Integer resourceId = byId.getResourceId();
        MeetingResourcePO byId1 = meetingResourceDaoService.getById(resourceId);
        Integer vmrMode = byId.getVmrMode();
        String ownerImUserId = byId.getOwnerImUserId();
        String hwMeetingCode = byId.getHwMeetingCode();
        String vmrId = byId1.getVmrId();

        meetingRoomInfoDaoService.removeById(meetingRoomId);
        //直接取消华为云会议
        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode))
            .cancelMeetingRoom(new CancelMeetingRoomModel(ownerImUserId, hwMeetingCode, vmrId));

        //取消资源占用
        publicResourceHoldHandle(resourceId, MeetingResourceHandleEnum.HOLD_DOWN);
        return CommonResult.success(null);
    }

    public void publicResourceHoldHandle(Integer resourceId, MeetingResourceHandleEnum meetingResourceHandleEnum) {
        log.info("当前资源：{} ，进行挂起释放操作：{}", resourceId, meetingResourceHandleEnum);
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(resourceId);
        Integer status = meetingResourcePO.getStatus();
        if (MeetingResourceStateEnum.PRIVATE.getState().equals(status)) {
            log.info("当前资源：{} 为私有资源，无需进行挂起释放操作", resourceId);
            return;
        }

        boolean subscribeFlag = MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState().equals(status);
        switch (meetingResourceHandleEnum) {
            case HOLD_UP:
                //置为公有预约
                boolean update = meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceId)
                    .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PUBLIC_FREE.getState())
                    .set(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState()).update();
                log.info("修改资源状态为公有预约状态结果：{}", update);
                break;
            case HOLD_DOWN:
                //要释放的情况，状态是共有预约或者共有预分配状态
                //查询是否有会议室占用该资源，如果没有则修改状态置为共有空闲或者私有
                Long count = meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                    .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).count();
                if (count == 0) {
                    //当前无占用会议室
                    boolean update1 = meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getId, resourceId)
                        //当前状态为共有预约。释放资源后，如果
                        .set(MeetingResourcePO::getStatus,
                            subscribeFlag ? MeetingResourceStateEnum.PUBLIC_FREE.getState()
                                : MeetingResourceStateEnum.PRIVATE.getState()).update();
                    log.info("修改资源状态为公有空闲状态结果：{}", update1);
                } else {
                    log.error("修改资源状态为公有预约状态失败无法释放,resourceId：{},count：{}", count);
                }
                break;
            default:
        }

    }

    /**
     * 首页查询即将召开和进行中的会议列表
     *
     * @return
     */
    @Override
    public CommonResult<FutureAndRunningMeetingRoomListVO> getFutureAndRunningMeetingRoomList(String imUserId) {
//        列表中显示最近要开始的会议，按照会议开始时间正序。最多显示30天数据
//        已结束的会议不显示。
//        我创建的 和 我参加的。
        List<MeetingRoomInfoPO> list =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getOwnerImUserId, imUserId)
                .in(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState(),
                    MeetingRoomStateEnum.Created.getState()).orderByDesc(MeetingRoomInfoPO::getLockStartTime)
                .last(" limit 30").list();
        //
        FutureAndRunningMeetingRoomListVO futureAndRunningMeetingRoomListVO =
            packFutureAndRunningMeetingRoomListVO(list);
        // hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.CLOUD.getVmrMode()).queryMeetingRoomList(imUserId);

        return CommonResult.success(futureAndRunningMeetingRoomListVO);
    }

    private FutureAndRunningMeetingRoomListVO packFutureAndRunningMeetingRoomListVO(List<MeetingRoomInfoPO> list) {
        FutureAndRunningMeetingRoomListVO futureAndRunningMeetingRoomListVO = new FutureAndRunningMeetingRoomListVO();
       /*  List<MeetingRoomDetailDTO> todayRooms = Lists.newArrayList();
        List<MeetingRoomDetailDTO> tomorrowRooms = Lists.newArrayList();
        List<MeetingRoomDetailDTO> otherRooms = Lists.newArrayList();
        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            Date lockStartTime = meetingRoomInfoPO.getLockStartTime();
            //今天
            DateTime today = DateUtil.date();
            DateTime todayBegin = DateUtil.beginOfDay(today);
            DateTime tomorrow = DateUtil.tomorrow();
            DateTime tomorrowBegin = DateUtil.beginOfDay(tomorrow);
            //日期是今天
            if (todayBegin.equals(DateUtil.beginOfDay(lockStartTime))) {
                todayRooms.add(packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, false));
            } else if (tomorrowBegin.equals(DateUtil.beginOfDay(lockStartTime))) {
                //日期是明天
                tomorrowRooms.add(packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, false));
            } else {
                //日期是其他
                otherRooms.add(packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, false));
            }
        }
        //三者排序规则一样
        todayRooms = todayRooms.stream().sorted(Comparator.comparing(MeetingRoomDetailDTO::getLockStartTime))
            .collect(Collectors.toList());
        tomorrowRooms = tomorrowRooms.stream().sorted(Comparator.comparing(MeetingRoomDetailDTO::getLockStartTime))
            .collect(Collectors.toList());
        otherRooms = otherRooms.stream().sorted(Comparator.comparing(MeetingRoomDetailDTO::getLockStartTime))
            .collect(Collectors.toList());

        futureAndRunningMeetingRoomListVO.setTodayRooms(todayRooms);
        futureAndRunningMeetingRoomListVO.setTomorrowRooms(tomorrowRooms);
        futureAndRunningMeetingRoomListVO.setOtherRooms(otherRooms);*/
        List<MeetingRoomDetailDTO> meetingRoomDetailDTOS = BeanUtil.copyToList(list, MeetingRoomDetailDTO.class);
        TreeMap<String, List<MeetingRoomDetailDTO>> sortMap = new TreeMap<>(Comparator.comparing(DateUtil::parse));
        Map<String, List<MeetingRoomDetailDTO>> collect = meetingRoomDetailDTOS.stream().collect(
            Collectors.groupingBy(f -> DateUtil.format(f.getLockStartTime(), DatePattern.NORM_DATE_PATTERN),
                () -> sortMap, Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(MeetingRoomDetailDTO::getLockStartTime))),
                    Lists::newArrayList)));
        futureAndRunningMeetingRoomListVO.setRooms(collect);
        return futureAndRunningMeetingRoomListVO;
    }



    /**
     * @return
     */
    @Override
    public CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList(String imUserId, Integer month) {
        DateTime dateTime = getMonth(month);
        DateTime start = DateUtil.beginOfMonth(dateTime);
        DateTime end = DateUtil.endOfMonth(dateTime);
        List<MeetingRoomInfoPO> list =
            meetingRoomInfoDaoService.lambdaQuery().ge(MeetingRoomInfoPO::getLockStartTime, start)
                .le(MeetingRoomInfoPO::getLockEndTime, end)
                .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
                .eq(MeetingRoomInfoPO::getOwnerImUserId, imUserId).orderByDesc(MeetingRoomInfoPO::getCreateTime).list();
        List<MeetingRoomDetailDTO> collect =
            list.stream().map(t -> packBaseMeetingRoomDetailDTO(t, false)).collect(Collectors.toList());
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
        Integer resourceId = availableResourcePeriodGetDTO.getResourceId();

        //查询该资源当日占用情况
        List<FreeTimeCalculatorUtil.TimeRange> collect =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getResourceId, resourceId)
                .between(MeetingRoomInfoPO::getLockStartTime, DateUtil.beginOfDay(date), DateUtil.endOfDay(date))
                .between(MeetingRoomInfoPO::getLockEndTime, DateUtil.beginOfDay(date), DateUtil.endOfDay(date))
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list().stream()
                .map(t -> new FreeTimeCalculatorUtil.TimeRange(t.getLockStartTime(), t.getLockEndTime()))
                .collect(Collectors.toList());
        List<FreeTimeCalculatorUtil.TimeRange> timeRanges = FreeTimeCalculatorUtil.calculateFreeTimeRanges(collect);
        List<AvailableResourcePeriodVO> result =
            timeRanges.stream().map(t -> new AvailableResourcePeriodVO(t.getStart().toString(), t.getEnd().toString()))
                .collect(Collectors.toList());
        return CommonResult.success(result);
    }

    /**
     * 更新华为云会议室状态
     *
     * @param hwEventReq
     * @return
     */
    @Override
    public CommonResult<String> updateMeetingRoomStatus(HwEventReq hwEventReq) {
        log.info("企业级华为云事件推送入入参：{}", hwEventReq);
        String nonce = hwEventReq.getNonce();
        EventInfo eventInfo = hwEventReq.getEventInfo();
        //事件名
        String event = eventInfo.getEvent();
        if ("meeting.verify".equals(event)) {
            //验证事件
            String s = JSONUtil.createObj().set("event", "meeting.verify").set("nonce", nonce).toStringPretty();
            return CommonResult.success(s);
        }

        Long timestamp = eventInfo.getTimestamp();
        Payload payload = eventInfo.getPayload();
        String meetingID = payload.getMeetingInfo().getMeetingID();
        Optional<MeetingRoomInfoPO> meetingRoomInfoPOOptional =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getHwMeetingCode, meetingID).oneOpt();
        if (!meetingRoomInfoPOOptional.isPresent()) {
            log.error("事件回调数据异常，数据不存在 meetingID：{}", meetingID);
            return CommonResult.success("");
        }
        MeetingRoomInfoPO meetingRoomInfoPO = meetingRoomInfoPOOptional.get();
        MeetingResourcePO meetingResourcePO = meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());

        if ("meeting.started".equals(event)) {
            //推送会议开始事件
            boolean update = meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getHwMeetingCode, meetingID)
                .eq(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Schedule.getState())
                .set(MeetingRoomInfoPO::getHwMeetingId, payload.getMeetingInfo().getMeetingUUID())
                .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Created.getState())
                .set(MeetingRoomInfoPO::getRelStartTime, DateUtil.date(timestamp)).update();
            log.info("华为云会议事件开始会议id：{}，结果：{}", meetingID, update);
        } else if ("meeting.end".equals(event)) {
            //会议结束事件-当企业下的某个会议结束，服务端会推送会议结束事件消息的post请求到企业开发者回调URL。会议结束后，如果会议预定的结束时间还没到，可以再次加入该会议。
        } else if ("meeting.conclude".equals(event)) {
            //会议关闭事件
            boolean update = meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getHwMeetingCode, meetingID)
                .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState())
                .set(MeetingRoomInfoPO::getRelEndTime, DateUtil.date(timestamp)).update();

            //回收资源
            publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(), MeetingResourceHandleEnum.HOLD_DOWN);

            hwMeetingCommonService.disassociateVmr(meetingRoomInfoPO.getOwnerImUserId(),
                Collections.singletonList(meetingResourcePO.getVmrId()));

            log.info("华为云会议结束修改会议id：{}，结果：{}", meetingID, update);
        } else if ("record.finish".equals(event)) {
            //录制结束事件-当企业下的某个会议结束，服务端会推送录制结束事件消息的post请求到企业开发者回调URL
            boolean update = meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getHwMeetingCode, meetingID)
                .set(MeetingRoomInfoPO::getRecordStatus, 1).update();
        }

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
    public CommonResult<List<ResourceTypeVO>> getMeetingResourceTypeList(String imUserId, Integer levelCode) {
        //获取最大会议用户等级
        Integer maxResourceType = getMaxLevel(levelCode, imUserId);
        //根据资源等级过滤资源类型
        List<ResourceTypeVO> levelResourceTypeVOList =
            Arrays.stream(MeetingResourceEnum.values()).filter(t -> t.getCode() != 0 && t.getCode() <= maxResourceType)
                .collect(Collectors.toList()).stream()
                .map(t -> ResourceTypeVO.builder().code(String.valueOf(t.getCode())).type(1).desc(t.getDesc()).build())
                .collect(Collectors.toList());
        //查询私池
        String privateResourceTypeFormat = "专属会议室（适用于%d人以下）";
        List<MeetingResourcePO> privateResourceList =
            meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getOwnerImUserId, imUserId)
                .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState()).list();

        List<ResourceTypeVO> collect = privateResourceList.stream().map(MeetingResourcePO::getSize).distinct().map(
            t -> ResourceTypeVO.builder().type(2).code(imUserId + "-" + t)
                .desc(String.format(privateResourceTypeFormat, t)).build()).collect(Collectors.toList());

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
        //是数字
        if (NumberUtil.isNumber(resourceCode)) {
            result = meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getResourceType, resourceCode)
                .ne(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState())
                .ne(MeetingResourcePO::getStatus, MeetingResourceStateEnum.REDISTRIBUTION.getState()).list().stream()
                .collect(Collectors.toList());

        } else {
            String[] split = resourceCode.split("-");
            String imUserId = split[0];
            String resourceSize = split[1];
            result = meetingResourceDaoService.lambdaQuery().eq(MeetingResourcePO::getResourceType, resourceCode)
                .eq(MeetingResourcePO::getOwnerImUserId, imUserId).eq(MeetingResourcePO::getSize, resourceSize).list()
                .stream().collect(Collectors.toList());

        }

        return CommonResult.success(BeanUtil.copyToList(result, MeetingResourceVO.class));
    }

    @Override
    public CommonResult<MeetingRoomDetailDTO> getMeetingRoomByCode(String meetingCode) {
        log.info("查询会议详情，meetingCode：{}", meetingCode);
        MeetingRoomInfoPO meetingRoomInfoPO =
            meetingRoomInfoDaoService.lambdaQuery().eq(MeetingRoomInfoPO::getHwMeetingCode, meetingCode).one();
        if (ObjectUtil.isNull(meetingRoomInfoPO)) {
            return CommonResult.success(null);
        }
        Integer vmrMode = meetingRoomInfoPO.getVmrMode();
        MeetingRoomDetailDTO result = packBaseMeetingRoomDetailDTO(meetingRoomInfoPO, true);
        hwMeetingRoomHandlers.get(MeetingRoomHandlerEnum.getHandlerNameByVmrMode(vmrMode)).setMeetingRoomDetail(result);
        return CommonResult.success(result);
    }

    DateTime getMonth(Integer month) {
        DateTime now = DateUtil.date();
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
