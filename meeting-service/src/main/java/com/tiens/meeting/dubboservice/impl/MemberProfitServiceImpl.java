package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.http.param.MediaType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.google.common.collect.Lists;
import com.tiens.api.dto.*;
import com.tiens.api.service.*;
import com.tiens.api.vo.*;
import com.tiens.meeting.dubboservice.common.entity.SyncCommonResult;
import com.tiens.meeting.dubboservice.common.entity.VMCoinsOperateModel;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.*;
import com.tiens.meeting.repository.service.*;
import com.tiens.meeting.util.VmUserUtil;
import common.constants.CommonProfitConfigConstants;
import common.enums.MemberLevelEnum;
import common.enums.PaidTypeEnum;
import common.enums.ProfitRecordStateEnum;
import common.enums.TerminalEnum;
import common.exception.ErrorCode;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.util.cache.CacheKeyUtil;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MemberProfitServiceImpl implements MemberProfitService {

    private final MeetingConfig meetingConfig;

    private final RedissonClient redissonClient;

    private final MeetingBlackUserDaoService meetingBlackUserDaoService;

    private final MeetingUserProfitOrderDaoService meetingUserProfitOrderDaoService;

    private final MeetingUserPaidProfitDaoService meetingUserPaidProfitDaoService;

    private final MeetingUserProfitRecordDaoService meetingUserProfitRecordDaoService;

    private final MeetingProfitCommonConfigDaoService meetingProfitCommonConfigDaoService;

    private final MemberProfitCacheService memberProfitCacheService;

    private final MeetingCacheService meetingCacheService;

    private final MeetingPaidSettingService meetingPaidSettingService;

    @Autowired
    RpcMeetingRoomService rpcMeetingRoomService;

    @Autowired
    RpcMeetingUserService rpcMeetingUserService;

    private final MeetingBlackRecordDaoService meetingBlackRecordDaoService;




    /**
     * 校验用户权益
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public CommonResult checkProfit(MeetingRoomContextDTO meetingRoomContextDTO) {
        log.info("【校验用户权益】入参：{}", JSON.toJSONString(meetingRoomContextDTO));
        String imUserId = meetingRoomContextDTO.getImUserId();

        String resourceType = meetingRoomContextDTO.getResourceType();

        Integer memberType = meetingRoomContextDTO.getMemberType();

        String timeZoneOffset = meetingRoomContextDTO.getTimeZoneOffset();

        Boolean isHighestMemberLevel = MemberLevelEnum.BLUE.getState().equals(memberType);

        if (!memberProfitCacheService.getMemberProfitEnabled() || !NumberUtil.isNumber(resourceType)) {
            //海外用户或者私有会议返回成功
            return CommonResult.success(null);
        }

        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);
        Date showStartTime =
            DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(meetingRoomContextDTO.getStartTime(), now),
                DateUtils.TIME_ZONE_DEFAULT);

        String showStartTimeStr = DateUtil.format(showStartTime, "yyyy-MM-dd");

//        DateTime lockStartTime = DateUtil.offsetMinute(showStartTime, -leadTime);

        //校验用户权益
        //1、校验用户权益规则
        //1.2、首先查询会员权益配置，判断会员权益对应场次是否足够

        RMap<Integer, MeetingMemeberProfitConfigPO> map =
            redissonClient.getMap(CacheKeyUtil.getMemberProfitConfigKey());

        MeetingMemeberProfitConfigPO meetingMemeberProfitConfigPO = map.get(meetingRoomContextDTO.getMemberType());
        //每天会员免费几场
        Integer freeDayAppointCount = meetingMemeberProfitConfigPO.getFreeDayAppointCount();

        List<MeetingUserProfitRecordPO> meetingUserProfitRecordPOList =
            meetingUserProfitRecordDaoService.lambdaQuery().eq(MeetingUserProfitRecordPO::getUserId, imUserId)
                .eq(MeetingUserProfitRecordPO::getPaidType, PaidTypeEnum.MEMBER_FREE.getState())
                .apply("use_time = {0}", showStartTimeStr)
                //生效和预占用
                .in(MeetingUserProfitRecordPO::getStatus, Lists.newArrayList(ProfitRecordStateEnum.VALID.getState(),
                    ProfitRecordStateEnum.PRE_LOCK.getState())).list();

        long memberMeetingCount = meetingUserProfitRecordPOList.stream()
            .filter(t -> PaidTypeEnum.MEMBER_FREE.getState().equals(t.getPaidType())).count();
        if (memberMeetingCount < freeDayAppointCount) {
            log.info("【校验用户权益】会员免费权益可用：会员权益次数：{},当前已用权益次数：{}", freeDayAppointCount,
                memberMeetingCount);
            //免费次数未用完
            meetingRoomContextDTO.setPaidType(PaidTypeEnum.MEMBER_FREE.getState());
        } else {
            //免费次数用完了，需要校验付费次数
            //2.1、判断付费剩余时长是否足够,不足则返回错误

            //找到该资源类型的付费权益
            Optional<MeetingUserPaidProfitPO> meetingUserPaidProfitOpt =
                meetingUserPaidProfitDaoService.lambdaQuery().eq(MeetingUserPaidProfitPO::getUserId, imUserId)
                    .eq(MeetingUserPaidProfitPO::getResourceType, meetingRoomContextDTO.getResourceType()).oneOpt();

            if (!meetingUserPaidProfitOpt.isPresent()) {
                //无资源
                log.info("【校验用户权益】付费权益尚未购买：资源类型：{}", meetingRoomContextDTO.getResourceType());
                return CommonResult.error(getMemberProfitErrorCode(isHighestMemberLevel));
            }

            List<MeetingUserProfitRecordPO> meetingUserProfitPaidRecordPOList =
                meetingUserProfitRecordDaoService.lambdaQuery().eq(MeetingUserProfitRecordPO::getUserId, imUserId)
                    .eq(MeetingUserProfitRecordPO::getPaidType, PaidTypeEnum.PAID.getState())
                    .eq(MeetingUserProfitRecordPO::getResourceType, resourceType)
                    //预占用
                    .eq(MeetingUserProfitRecordPO::getStatus, ProfitRecordStateEnum.PRE_LOCK.getState()).list();

            //查询当前预占用
            AtomicReference<Integer> calDuration = new AtomicReference<>(0);
            meetingUserProfitPaidRecordPOList.stream().forEach(b -> {
                Integer lockDuration = b.getLockDuration();
                calDuration.updateAndGet(v -> v + lockDuration);
            });

            MeetingUserPaidProfitPO meetingUserPaidProfitPO = meetingUserPaidProfitOpt.get();

            Integer duration = meetingUserPaidProfitPO.getDuration();

            if (duration - calDuration.get() <= 0) {
                //无资源
                log.info("【校验用户权益】付费权益不足，无法预约会议：已购买付费时长：{}，当前占用时长：{}", duration,
                    calDuration.get());
                return CommonResult.error(getMemberProfitErrorCode(isHighestMemberLevel));
            }
            //有剩余资源时长
            meetingRoomContextDTO.setPaidType(PaidTypeEnum.PAID.getState());
        }

        return CommonResult.success(null);

    }

    ErrorCode getMemberProfitErrorCode(Boolean isHighestMemberLevel) {
        ErrorCode errorCode;
        if (isHighestMemberLevel) {
            errorCode = GlobalErrorCodeConstants.NEED_PAID;
        } else {
            errorCode = GlobalErrorCodeConstants.NEED_MEMBER_OR_PAID;
        }

      /*  if (isHighestMemberLevel) {
            errorCode = GlobalErrorCodeConstants.RESOURCE_MORE_THAN;
        } else {
            errorCode = GlobalErrorCodeConstants.NEED_MEMBER;
        }*/
        return errorCode;
    }

    /**
     * 保存权益记录
     *
     * @param meetingRoomContextDTO
     * @param meetingId
     * @return
     */
    @Override
    public CommonResult saveUserProfitRecord(MeetingRoomContextDTO meetingRoomContextDTO, Long meetingId) {
        if (memberProfitCacheService.getMemberProfitEnabled() && NumberUtil.isNumber(
            meetingRoomContextDTO.getResourceType())) {
            log.info("保存权益记录入参：meetingRoomContextDTO：{}，meetingId：{}", JSON.toJSONString(meetingRoomContextDTO),
                meetingId);
            DateTime showStartTime = DateUtils.roundToHalfHour(
                ObjectUtil.defaultIfNull(meetingRoomContextDTO.getStartTime(),
                    DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT)), DateUtils.TIME_ZONE_DEFAULT);
            String showStartTimeStr = DateUtil.format(showStartTime, "yyyy-MM-dd");
            //4、设置权益存储
            //校验通过之后，设置使用记录
            MeetingUserProfitRecordPO meetingUserProfitRecordPO = new MeetingUserProfitRecordPO();
            meetingUserProfitRecordPO.setUserId(meetingRoomContextDTO.getImUserId());
            meetingUserProfitRecordPO.setJoyoCode(meetingRoomContextDTO.getJoyoCode());
            meetingUserProfitRecordPO.setInitMemberType(meetingRoomContextDTO.getMemberType());
//                meetingUserProfitRecordPO.setCurrentMemberType(meetingRoomContextDTO.get);
            meetingUserProfitRecordPO.setPaidType(meetingRoomContextDTO.getPaidType());
            meetingUserProfitRecordPO.setUseTime(showStartTimeStr);
            meetingUserProfitRecordPO.setMeetingId(meetingId);
//        meetingUserProfitRecordPO.setRel_duration();
            meetingUserProfitRecordPO.setLockDuration(meetingRoomContextDTO.getLength());
            meetingUserProfitRecordPO.setResourceType(Integer.valueOf(meetingRoomContextDTO.getResourceType()));
            meetingUserProfitRecordPO.setStatus(ProfitRecordStateEnum.PRE_LOCK.getState());

            boolean save = meetingUserProfitRecordDaoService.save(meetingUserProfitRecordPO);

            return CommonResult.success(save);
        }
        return CommonResult.success(false);
    }

    /**
     * 查询首页头图展示
     *
     * @return
     */
    @Override
    public CommonResult<CmsShowVO> getCmsShow(CmsShowGetDTO cmsShowGetDTO) {
        if (!memberProfitCacheService.getCmsShowEnabled()) {
            return CommonResult.success(null);
        }
        Integer deviceType = cmsShowGetDTO.getDeviceType();
        TerminalEnum byTerminal = TerminalEnum.getByTerminal(deviceType);
        Boolean isCn = "zh-CN".equals(cmsShowGetDTO.getLanguageId());
        String defaultHwNation = "EN";
        String defaultZhNation = "CN";
        String deviceSuggestion = null;
        CmsShowVO cmsShowVO = new CmsShowVO();
        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());

        if (!isCn) {
            deviceSuggestion = map.get(byTerminal.name() + "_" + defaultHwNation);
        } else {
            deviceSuggestion = map.get(byTerminal.name() + "_" + defaultZhNation);
        }

        CommonResult<List<UserMemberProfitEntity>> listCommonResult = queryUserProfitConfig();

        cmsShowVO.setUserMemberProfitEntityList(listCommonResult.getData());

        cmsShowVO.setDeviceSuggestion(deviceSuggestion);
        return CommonResult.success(cmsShowVO);
    }

    /**
     * 推送订单
     *
     * @param pushOrderDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult pushOrder(PushOrderDTO pushOrderDTO) {

        log.info("订单推送入参，订单数据：{}", JSON.toJSONString(pushOrderDTO));
        CommonResult<VMUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryVMUser(pushOrderDTO.getJoyoCode(), "");
        VMUserVO data = vmUserVOCommonResult.getData();
        if (ObjectUtil.isNull(data)) {
            log.error("【订单推送】查询用户异常，data：{}", pushOrderDTO);
            return CommonResult.errorMsg("用户不存在");
        }
        String accid = data.getAccid();

        Integer resourceType = pushOrderDTO.getResourceType();
        Integer duration = pushOrderDTO.getDuration();

        //存储订单数据
        MeetingUserProfitOrderPO meetingUserProfitOrderPO = new MeetingUserProfitOrderPO();
        meetingUserProfitOrderPO.setUserId(accid);
        meetingUserProfitOrderPO.setJoyoCode(data.getJoyoCode());
        meetingUserProfitOrderPO.setOrderNo(pushOrderDTO.getOrderNo());
        meetingUserProfitOrderPO.setSkuId(pushOrderDTO.getSkuId());
        meetingUserProfitOrderPO.setOrderStatus(pushOrderDTO.getOrderStatus());
        meetingUserProfitOrderPO.setPaidAmount(pushOrderDTO.getPaidVmAmount());
        meetingUserProfitOrderPO.setResourceType(pushOrderDTO.getResourceType());
        //        meetingUserProfitOrderPO.setResourceDesc();
        meetingUserProfitOrderPO.setDuration(pushOrderDTO.getDuration());

        try {
            meetingUserProfitOrderDaoService.save(meetingUserProfitOrderPO);

            //增加会员付费权益
            MeetingUserPaidProfitPO meetingUserPaidProfitPO = new MeetingUserPaidProfitPO();
            meetingUserPaidProfitPO.setUserId(accid);
            meetingUserPaidProfitPO.setJoyoCode(data.getJoyoCode());
            meetingUserPaidProfitPO.setResourceType(resourceType);
            meetingUserPaidProfitPO.setDuration(pushOrderDTO.getDuration());

            Optional<MeetingUserPaidProfitPO> meetingUserPaidProfitOpt =
                meetingUserPaidProfitDaoService.lambdaQuery().eq(MeetingUserPaidProfitPO::getUserId, accid)
                    .eq(MeetingUserPaidProfitPO::getResourceType, resourceType).oneOpt();
            if (meetingUserPaidProfitOpt.isPresent()) {
                UpdateChainWrapper<MeetingUserPaidProfitPO> update = meetingUserPaidProfitDaoService.update();
                update.eq("user_id", accid).eq("resource_type", resourceType).setSql("duration=duration+" + duration)
                    .update();

            } else {
                //插入
                meetingUserPaidProfitDaoService.save(meetingUserPaidProfitPO);
            }

        } catch (DuplicateKeyException e) {
            log.error("订单重复录入异常，订单数据：{}", JSON.toJSONString(pushOrderDTO));
        }

        return CommonResult.success(null);
    }

    /**
     * 查询黑名单用户
     *
     * @param finalUserId
     * @return
     */
    @Override
    public CommonResult<MeetingBlackUserVO> getBlackUser(String finalUserId) {
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);

        //校验黑名单
        List<MeetingBlackUserPO> blackUserPOList =
            meetingBlackUserDaoService.lambdaQuery().eq(MeetingBlackUserPO::getUserId, finalUserId)
                .le(MeetingBlackUserPO::getStartTime, now).ge(MeetingBlackUserPO::getEndTime, now).list();

        if (ObjectUtil.isNotEmpty(blackUserPOList)) {
            MeetingBlackUserPO meetingBlackUserPO = blackUserPOList.get(0);
            MeetingBlackUserVO meetingBlackUserVO =
                BeanUtil.copyProperties(meetingBlackUserPO, MeetingBlackUserVO.class);
            MeetingConfig.BlackUserConfigInner blackUserConfig = meetingConfig.getBlackUserConfig();

            meetingBlackUserVO.setMaxTime(blackUserConfig.getMaxTime());
            meetingBlackUserVO.setLockDay(blackUserConfig.getLockDay());
            return CommonResult.success(meetingBlackUserVO);

        }
        return CommonResult.success(null);
    }


    @Override
    public CommonResult<List<MeetingPaidSettingVO>> getMeetingPaidSettingList() {
        List<MeetingPaidSettingPO> list = meetingPaidSettingService.list();
        return CommonResult.success(BeanUtil.copyToList(list, MeetingPaidSettingVO.class));
    }

    @Override
    public CommonResult updMeetingPaidSetting(MeetingPaidSettingVO request) {
        if (request.getId() == null) {
            MeetingPaidSettingPO one = meetingPaidSettingService.getOne(new LambdaQueryWrapper<MeetingPaidSettingPO>()
                    .eq(MeetingPaidSettingPO::getResourceType, request.getResourceType()));
            if (one != null) {
                return CommonResult.errorMsg("当前资源类型已经存在，请配置其他类型~");
            }
        }
        MeetingPaidSettingPO settingPo = BeanUtil.copyProperties(request, MeetingPaidSettingPO.class);
        return CommonResult.success(meetingPaidSettingService.saveOrUpdate(settingPo));
    }

    /**
     * 查询用户权益
     *
     * @param finalUserId
     * @param memberType
     * @return
     */
    @Override
    public CommonResult<MeetingUserProfitVO> getUserProfit(String finalUserId, Integer memberType) {
        MeetingUserProfitVO meetingUserProfitVO = new MeetingUserProfitVO();
        RMap<Integer, MeetingMemeberProfitConfigPO> map =
            redissonClient.getMap(CacheKeyUtil.getMemberProfitConfigKey());

        MeetingMemeberProfitConfigPO meetingMemeberProfitConfigPO = map.get(memberType);

        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_DEFAULT);

        String todayStr = DateUtil.format(now, "yyyy-MM-dd");

        Long useCount =
            meetingUserProfitRecordDaoService.lambdaQuery().eq(MeetingUserProfitRecordPO::getUserId, finalUserId)
                .apply("use_time = {0}", todayStr)
                .eq(MeetingUserProfitRecordPO::getPaidType, PaidTypeEnum.MEMBER_FREE.getState())
                //生效和预占用
                .in(MeetingUserProfitRecordPO::getStatus, Lists.newArrayList(ProfitRecordStateEnum.VALID.getState(),
                    ProfitRecordStateEnum.PRE_LOCK.getState())).count();

        meetingUserProfitVO.setUserMemberProfit(
            this.packMeetingMemberProfitConfigPO(meetingMemeberProfitConfigPO, useCount));

        List<UserPaidProfitEntity> userPaidProfits = getUserPaidProfit(finalUserId);
        meetingUserProfitVO.setUserPaidProfits(userPaidProfits);

        return CommonResult.success(meetingUserProfitVO);
    }

    private List<UserPaidProfitEntity> getUserPaidProfit(String finalUserId) {
        List<MeetingUserPaidProfitPO> list =
            meetingUserPaidProfitDaoService.lambdaQuery().eq(MeetingUserPaidProfitPO::getUserId, finalUserId)
                .ne(MeetingUserPaidProfitPO::getDuration, 0).list();
        List<UserPaidProfitEntity> collect = list.stream().map(t -> {
            UserPaidProfitEntity userPaidProfitEntity = new UserPaidProfitEntity();
            userPaidProfitEntity.setResourceType(t.getResourceType());
            userPaidProfitEntity.setAccumulatedDuration(t.getDuration());

            return userPaidProfitEntity;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 查询权益配置
     *
     * @return
     */
    @Override
    public CommonResult<List<UserMemberProfitEntity>> queryUserProfitConfig() {
        RMap<Integer, MeetingMemeberProfitConfigPO> map =
            redissonClient.getMap(CacheKeyUtil.getMemberProfitConfigKey());
        Collection<MeetingMemeberProfitConfigPO> values = map.values();

        List<UserMemberProfitEntity> collect = values.stream().map(t -> packMeetingMemberProfitConfigPO(t, null))
            .sorted(Comparator.comparing(UserMemberProfitEntity::getMemberType)).collect(Collectors.toList());
        log.info("【查询权益配置】 返回：{}", JSON.toJSONString(collect));
        return CommonResult.success(collect);
    }

    UserMemberProfitEntity packMeetingMemberProfitConfigPO(MeetingMemeberProfitConfigPO t, Long useCount) {
        UserMemberProfitEntity userMemberProfitEntity = new UserMemberProfitEntity();
        userMemberProfitEntity.setMemberType(t.getMemberType());

        Integer freeDayAppointCount = t.getFreeDayAppointCount();

        userMemberProfitEntity.setFreeDayAppointCount(
            ObjectUtil.isNotNull(useCount) ? (int)Math.abs(freeDayAppointCount - useCount) : freeDayAppointCount);
        userMemberProfitEntity.setEveryLimitCount(t.getLimitCount());

        return userMemberProfitEntity;

    }

    /**
     * 修改会员权益
     *
     * @param userMemberProfitModifyEntity
     * @return
     */
    @Override
    @Transactional
    public CommonResult modUserMemberProfit(UserMemberProfitModifyEntity userMemberProfitModifyEntity) {

        log.info("【修改会员权益】 入参：{}", JSON.toJSONString(userMemberProfitModifyEntity));
        // 当前UTC时间
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
        String dateNow = now.toDateStr();

        //重置当日会员权益,,将当日会员使用记录失效
        Integer getType = userMemberProfitModifyEntity.getGetType();

        //升级
        UpdateWrapper<MeetingUserProfitRecordPO> update = Wrappers.update();
        update.eq("use_time", dateNow);
        update.eq("user_id", userMemberProfitModifyEntity.getAccId());
//            update.eq("status", ProfitRecordStateEnum.VALID.getState());
        update.set("status", ProfitRecordStateEnum.INVALID.getState());
        meetingUserProfitRecordDaoService.update(update);

        //刷新用户缓存
        meetingCacheService.refreshMeetingUserCache(userMemberProfitModifyEntity.getAccId(), null);
        return CommonResult.success(null);
    }

    /**
     * 结算会员权益
     *
     * @param meetingId
     * @param imUserId
     * @param resourceType
     * @param languageId
     * @param betweenMinutes
     * @return
     */
    @Override
    @Transactional
    public CommonResult settleMemberProfit(Long meetingId, String imUserId, String resourceType, String languageId,
        long betweenMinutes) {

        log.info("【会员会议权益结算】入参meetingId：{}.imUserId:{}", meetingId, imUserId);
        if (!memberProfitCacheService.getMemberProfitEnabled() || !NumberUtil.isNumber(resourceType)) {
            //私有会议直接返回
            return CommonResult.success(null);
        }
        RLock lock = redissonClient.getLock(CacheKeyUtil.getSettleProfitKey(meetingId));
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Optional<MeetingUserProfitRecordPO> meetingUserProfitRecordOpt =
                meetingUserProfitRecordDaoService.lambdaQuery().eq(MeetingUserProfitRecordPO::getMeetingId, meetingId)
                    .oneOpt();

            if (meetingUserProfitRecordOpt.isPresent() && ObjectUtil.isNotNull(
                meetingUserProfitRecordOpt.get().getRelDuration())) {
                //已经设置过，无需设置
                log.info("【会员会议权益结算】入参meetingId：{}.imUserId:{},重复设置", meetingId, imUserId);
                return CommonResult.success(null);
            }

            //修改会员收益记录的状态为生效,设置真实结束时间
            boolean update =
                meetingUserProfitRecordDaoService.lambdaUpdate().eq(MeetingUserProfitRecordPO::getMeetingId, meetingId)
                    .eq(MeetingUserProfitRecordPO::getUserId, imUserId)
                    .set(MeetingUserProfitRecordPO::getStatus, ProfitRecordStateEnum.VALID.getState())
                    .set(MeetingUserProfitRecordPO::getRelDuration, betweenMinutes).update();

            Optional<MeetingUserPaidProfitPO> meetingUserPaidProfitOpt =
                meetingUserPaidProfitDaoService.lambdaQuery().eq(MeetingUserPaidProfitPO::getUserId, imUserId)
                    .eq(MeetingUserPaidProfitPO::getResourceType, resourceType).oneOpt();
            boolean update2 = false;
            if (meetingUserPaidProfitOpt.isPresent()) {
                //扣减付费权益
                MeetingUserPaidProfitPO meetingUserPaidProfitPO = meetingUserPaidProfitOpt.get();
                Integer duration = meetingUserPaidProfitPO.getDuration();
                long min = Math.min(duration, betweenMinutes);
                //如果有付费权益，则修改剩余时间
                UpdateChainWrapper<MeetingUserPaidProfitPO> update1 = meetingUserPaidProfitDaoService.update();
                update2 =
                    update1.eq("user_id", imUserId).eq("resource_type", resourceType).setSql("duration=duration-" + min)
                        .update();
            }

            log.info("【会员会议权益结算】入参meetingId：{},执行结果1:{}执行结果2:{}", meetingId, update, update2);
            return CommonResult.success(null);
        } catch (Exception e) {
            log.error("【会员会议权益结算】 结算异常，入参meetingId：{}.imUserId:{}", meetingId, imUserId, e);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        return CommonResult.success(null);
    }

    /**
     * 保存通用权益配置
     *
     * @param commonProfitConfigSaveDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult saveCommonProfitConfig(CommonProfitConfigSaveDTO commonProfitConfigSaveDTO) {
        MeetingProfitCommonConfigPO cmsConfig = new MeetingProfitCommonConfigPO();
        cmsConfig.setConfigKey(CommonProfitConfigConstants.CMS_SHOW_FLAG);
        cmsConfig.setConfigValue(commonProfitConfigSaveDTO.getCmsShowFlag());

        MeetingProfitCommonConfigPO memberProfitFlag = new MeetingProfitCommonConfigPO();
        memberProfitFlag.setConfigKey(CommonProfitConfigConstants.MEMBER_PROFIT_FLAG);
        memberProfitFlag.setConfigValue(commonProfitConfigSaveDTO.getMemberProfitFlag());

        ArrayList<MeetingProfitCommonConfigPO> meetingProfitCommonConfigPOS =
            Lists.newArrayList(cmsConfig, memberProfitFlag);

        meetingProfitCommonConfigDaoService.saveOrUpdateBatch(meetingProfitCommonConfigPOS);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //刷新缓存
                log.info("执行刷新缓存逻辑");
                memberProfitCacheService.refreshMemberProfitCache();
            }
        });
        return CommonResult.success(null);
    }

    /**
     * 查询权益公共配置
     *
     * @return
     */
    @Override
    public CommonResult<CommonProfitConfigQueryVO> queryCommonProfitConfig() {
        List<UserMemberProfitEntity> userMemberProfitList = queryUserProfitConfig().getData();
        List<MeetingProfitCommonConfigPO> list = meetingProfitCommonConfigDaoService.lambdaQuery().list();

        CommonProfitConfigQueryVO commonProfitConfigQueryVO = new CommonProfitConfigQueryVO();
        commonProfitConfigQueryVO.setUserMemberProfitList(userMemberProfitList);
        commonProfitConfigQueryVO.setCmsShowFlag(
            list.stream().filter(t -> CommonProfitConfigConstants.CMS_SHOW_FLAG.equals(t.getConfigKey())).findAny()
                .get().getConfigValue());
        commonProfitConfigQueryVO.setMemberProfitFlag(
            list.stream().filter(t -> CommonProfitConfigConstants.MEMBER_PROFIT_FLAG.equals(t.getConfigKey())).findAny()
                .get().getConfigValue());

        return CommonResult.success(commonProfitConfigQueryVO);
    }

    /**
     * 查询权益商品列表
     *
     * @return
     */
    @Override
    public CommonResult<List<MeetingProfitProductListVO>> queryMeetingProfitProductList() {
        RMap<Integer, MeetingProfitProductListPO> map = redissonClient.getMap(CacheKeyUtil.getProfitProductListKey());
        Collection<MeetingProfitProductListPO> values = map.values();
        List<MeetingProfitProductListVO> meetingProfitProductListVOS =
            BeanUtil.copyToList(values, MeetingProfitProductListVO.class);
        return CommonResult.success(meetingProfitProductListVOS);
    }

    /**
     * 购买权益
     *
     * @param buyMeetingProfitDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult buyMeetingProfit(BuyMeetingProfitDTO buyMeetingProfitDTO) {

        log.info("【购买权益】入参：{}", JSON.toJSONString(buyMeetingProfitDTO));

        //1、查询商品信息
        String resourceType = buyMeetingProfitDTO.getResourceType();
        String nationId = buyMeetingProfitDTO.getNationId();
        String finalUserId = buyMeetingProfitDTO.getFinalUserId();
        String joyoCode = buyMeetingProfitDTO.getJoyoCode();

        MeetingProfitProductListPO meetingProfitProductListPO = queryMeetingProfitProductByCode(resourceType);
        if (ObjectUtil.isNull(meetingProfitProductListPO)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_PROFIT_PRODUCT);
        }
        FreeResourceListDTO freeResourceListDTO = new FreeResourceListDTO();
        freeResourceListDTO.setStartTime(buyMeetingProfitDTO.getStartTime());
        freeResourceListDTO.setLength(buyMeetingProfitDTO.getLength());
        freeResourceListDTO.setResourceType(buyMeetingProfitDTO.getResourceType());
        freeResourceListDTO.setTimeZoneOffset(buyMeetingProfitDTO.getTimeZoneOffset());
        //2、校验资源是否可用
        CommonResult<List<MeetingResourceVO>> freeResourceList =
            rpcMeetingRoomService.getFreeResourceList(freeResourceListDTO);

        if (freeResourceList.isError() || ObjectUtil.isEmpty(freeResourceList.getData())) {
            log.error("校验资源是否可用失败，数据为空，查询参数：{}", JSON.toJSONString(freeResourceList));
            return CommonResult.error(GlobalErrorCodeConstants.ERROR_BUY_PROFIT);
        }

        //3、扣减vm币
        CommonResult result1 =
            doCountDownMemberProfitCoins(joyoCode, nationId, meetingProfitProductListPO.getVmCoins());
        if (result1.isError()) {
            log.error("扣减VM币失败，错误信息：{}", result1.getMsg());
            return CommonResult.error(GlobalErrorCodeConstants.ERROR_BUY_PROFIT);
        }

        //4、存储订单和计算权益
        PushOrderDTO pushOrderDTO = new PushOrderDTO();
        pushOrderDTO.setNationId(nationId);
        pushOrderDTO.setAccId(finalUserId);
        pushOrderDTO.setJoyoCode(joyoCode);
        pushOrderDTO.setOrderNo(getProfitOrderNewNo());
        pushOrderDTO.setSkuId(meetingProfitProductListPO.getProfitCode());
        pushOrderDTO.setOrderStatus(1);
        pushOrderDTO.setPaidVmAmount(new BigDecimal(meetingProfitProductListPO.getVmCoins()));
        pushOrderDTO.setPaidRealAmount(new BigDecimal(meetingProfitProductListPO.getVmCoins()));
        pushOrderDTO.setResourceType(meetingProfitProductListPO.getResourceType());
        pushOrderDTO.setDuration(meetingProfitProductListPO.getDuration());

        CommonResult result = pushOrder(pushOrderDTO);
        if (result.isError()) {
            log.error("存储订单和计算权益失败，错误信息：{}", result.getMsg());
            return CommonResult.error(GlobalErrorCodeConstants.ERROR_BUY_PROFIT);
        }

        return CommonResult.success(null);
    }

    CommonResult doCountDownMemberProfitCoins(String joyoCode, String country, Integer amount) {
        log.info("【权益付费购买】入参joyoCode：{}， country：{}，amount：{}", joyoCode, country, amount);
        Integer memberProfitCoinsSource = meetingConfig.getMemberProfitCoinsSource();

        //https://yapi.tiens.com/project/575/interface/api/38207
        Map<String, String> authHeadByJoyoCode = VmUserUtil.getAuthHeadByJoyoCode(joyoCode);
        VMCoinsOperateModel vmCoinsOperateModel = new VMCoinsOperateModel();
        vmCoinsOperateModel.setCountry(country);
        vmCoinsOperateModel.setSource(1);
        vmCoinsOperateModel.setAmount(amount);
        vmCoinsOperateModel.setOperateType(2);
        vmCoinsOperateModel.setCoinSource(memberProfitCoinsSource);

        HttpResponse execute =
            HttpUtil.createPost(meetingConfig.getCountDownVMCoinsUrl()).addHeaders(authHeadByJoyoCode)
                .body(JSON.toJSONString(vmCoinsOperateModel), MediaType.APPLICATION_JSON).execute();
        String result = execute.body();
        log.info("【权益付费购买】 返回结果：{}", result);
        SyncCommonResult syncCommonResult = JSON.parseObject(result, SyncCommonResult.class);
        if (!syncCommonResult.getSuccess()) {
            return CommonResult.error(GlobalErrorCodeConstants.ERROR_BUY_PROFIT);
        } else {
            return CommonResult.success(null);

        }
    }

    public static void main(String[] args) {
        System.out.println(getProfitOrderNewNo());
    }

    static String getProfitOrderNewNo() {
        //MT202407110932+6位随机
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);
        String orderNo = "MT" + now.toString(new SimpleDateFormat("yyyyMMddHHmmss")) + RandomUtil.randomNumbers(6);

        return orderNo;
    }

    /**
     * 通过资源类型查询商品信息
     *
     * @param resourceType
     * @return
     */
    private MeetingProfitProductListPO queryMeetingProfitProductByCode(String resourceType) {
        if (!NumberUtil.isNumber(resourceType)) {
            return null;
        }
        RMap<Integer, MeetingProfitProductListPO> map = redissonClient.getMap(CacheKeyUtil.getProfitProductListKey());

        MeetingProfitProductListPO meetingProfitProductListPO = map.get(Integer.parseInt(resourceType));

        return meetingProfitProductListPO;
    }

}
