package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.google.common.collect.Lists;
import com.tiens.api.dto.CmsShowGetDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.dto.UserMemberProfitModifyEntity;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.*;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.*;
import com.tiens.meeting.repository.service.MeetingBlackUserDaoService;
import com.tiens.meeting.repository.service.MeetingUserPaidProfitDaoService;
import com.tiens.meeting.repository.service.MeetingUserProfitOrderDaoService;
import com.tiens.meeting.repository.service.MeetingUserProfitRecordDaoService;
import common.enums.MemberLevelEnum;
import common.enums.PaidTypeEnum;
import common.enums.ProfitRecordStateEnum;
import common.enums.TerminalEnum;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.util.cache.CacheKeyUtil;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    RpcMeetingUserService rpcMeetingUserService;

    /**
     * 校验用户权益
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public CommonResult checkProfit(MeetingRoomContextDTO meetingRoomContextDTO) {

        String imUserId = meetingRoomContextDTO.getImUserId();

        String resourceType = meetingRoomContextDTO.getResourceType();

        String languageId = meetingRoomContextDTO.getLanguageId();

        Integer memberType = meetingRoomContextDTO.getMemberType();

        Boolean isHighestMemberLevel = MemberLevelEnum.BLUE.getState().equals(memberType);

        if (!NumberUtil.isNumber(resourceType) || !"zh-CN".equals(languageId)) {
            //海外用户或者私有会议返回成功
            return CommonResult.success(null);
        }

        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);
        Date showStartTime =
            DateUtils.roundToHalfHour(ObjectUtil.defaultIfNull(meetingRoomContextDTO.getStartTime(), now),
                DateUtils.TIME_ZONE_GMT);

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
                .apply("use_time = {0}", showStartTimeStr)
                //生效和预占用
                .in(MeetingUserProfitRecordPO::getStatus, Lists.newArrayList(ProfitRecordStateEnum.VALID.getState(),
                    ProfitRecordStateEnum.PRE_LOCK.getState())).list();

        long memberMeetingCount = meetingUserProfitRecordPOList.stream()
            .filter(t -> PaidTypeEnum.MEMBER_FREE.getState().equals(t.getPaidType())).count();
        if (memberMeetingCount < freeDayAppointCount) {

            String str = "[\"15295765073\", \"1320002222\"]";
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
                return CommonResult.error(isHighestMemberLevel ? GlobalErrorCodeConstants.NEED_PAID
                    : GlobalErrorCodeConstants.NEED_MEMBER_OR_PAID);
            }

            //查询当前预占用和实际消耗时间
            AtomicReference<Integer> calDuration = new AtomicReference<>(0);
            meetingUserProfitRecordPOList.stream().filter(t -> PaidTypeEnum.PAID.getState().equals(t.getPaidType()))
                .forEach(b -> {
                    Integer lockDuration = b.getLockDuration();
                    Integer relDuration = b.getRelDuration();
                    Integer finalCalDuration = (ObjectUtil.isNotNull(relDuration) ? relDuration : lockDuration);

                    calDuration.updateAndGet(v -> v + finalCalDuration);
                });

            MeetingUserPaidProfitPO meetingUserPaidProfitPO = meetingUserPaidProfitOpt.get();

            Integer duration = meetingUserPaidProfitPO.getDuration();

            if (duration - calDuration.get() - meetingRoomContextDTO.getLength() <= 0) {
                //无资源
                return CommonResult.error(isHighestMemberLevel ? GlobalErrorCodeConstants.NEED_PAID
                    : GlobalErrorCodeConstants.NEED_MEMBER_OR_PAID);
            }
            //有剩余资源时长
            meetingRoomContextDTO.setPaidType(PaidTypeEnum.PAID.getState());
        }

        return CommonResult.success(null);

    }

    /**
     * 查询首页头图展示
     *
     * @return
     */
    @Override
    public CommonResult<CmsShowVO> getCmsShow(CmsShowGetDTO cmsShowGetDTO) {
        MeetingConfig.CmsShowConfigInner cmsShowConfig = meetingConfig.getCmsShowConfig();
        if (!cmsShowConfig.getEnable()) {
            return CommonResult.success(null);
        }
        Integer deviceType = cmsShowGetDTO.getDeviceType();
        TerminalEnum byTerminal = TerminalEnum.getByTerminal(deviceType);

        String deviceSuggestion = null;
        CmsShowVO cmsShowVO = new CmsShowVO();

        switch (byTerminal) {
            case ANDROID:
                deviceSuggestion = cmsShowConfig.getAndroidBaseConfig();
                break;
            case IOS:
                deviceSuggestion = cmsShowConfig.getIosBaseConfig();
                break;
            case WINDOWS:
                deviceSuggestion = cmsShowConfig.getWindowsBaseConfig();
                break;
            case MAC:
                deviceSuggestion = cmsShowConfig.getMacBaseConfig();
                break;
            default:
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

        log.error("订单推送入参，订单数据：{}", JSON.toJSONString(pushOrderDTO));
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
            return CommonResult.success(meetingBlackUserVO);

        }
        return CommonResult.success(null);
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

        meetingUserProfitVO.setUserMemberProfit(this.packMeetingMemberProfitConfigPO(meetingMemeberProfitConfigPO));
        List<UserPaidProfitEntity> userPaidProfits = getUserPaidProfit(finalUserId);
        meetingUserProfitVO.setUserPaidProfits(userPaidProfits);

        return CommonResult.success(meetingUserProfitVO);
    }

    private List<UserPaidProfitEntity> getUserPaidProfit(String finalUserId) {
        List<MeetingUserPaidProfitPO> list =
            meetingUserPaidProfitDaoService.lambdaQuery().eq(MeetingUserPaidProfitPO::getUserId, finalUserId).list();
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

        List<UserMemberProfitEntity> collect =
            values.stream().map(this::packMeetingMemberProfitConfigPO).collect(Collectors.toList());

        return CommonResult.success(collect);
    }

    UserMemberProfitEntity packMeetingMemberProfitConfigPO(MeetingMemeberProfitConfigPO t) {
        UserMemberProfitEntity userMemberProfitEntity = new UserMemberProfitEntity();
        userMemberProfitEntity.setMemberType(t.getMemberType());
        userMemberProfitEntity.setFreeDayAppointCount(t.getFreeDayAppointCount());
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
    public CommonResult settleMemberProfit(Long meetingId, String imUserId, String resourceType, String languageId,
        long betweenMinutes) {

        log.info("【会员会议权益结算】入参meetingId：{}.imUserId:{}", meetingId, imUserId);
        if (!NumberUtil.isNumber(resourceType) || !"zh-CN".equals(languageId)) {
            //私有会议直接返回
            return CommonResult.success(null);
        }

        //修改会员收益记录的状态为生效,设置真实结束时间
        boolean update =
            meetingUserProfitRecordDaoService.lambdaUpdate().eq(MeetingUserProfitRecordPO::getMeetingId, meetingId)
                .eq(MeetingUserProfitRecordPO::getUserId, imUserId)
                .set(MeetingUserProfitRecordPO::getStatus, ProfitRecordStateEnum.VALID.getState())
                .set(MeetingUserProfitRecordPO::getRelDuration, betweenMinutes).update();
        //如果有付费权益，则修改剩余时间

        UpdateChainWrapper<MeetingUserPaidProfitPO> update1 = meetingUserPaidProfitDaoService.update();
        boolean update2 = update1.eq("user_id", imUserId).eq("resource_type", resourceType)
            .setSql("duration=duration-" + betweenMinutes).update();

        log.info("【会员会议权益结算】入参meetingId：{},执行结果1:{}执行结果2:{}", meetingId, update, update2);
        return CommonResult.success(null);
    }
}
