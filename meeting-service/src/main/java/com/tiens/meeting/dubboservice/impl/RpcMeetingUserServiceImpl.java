package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import com.huaweicloud.sdk.meeting.v1.model.BatchDeleteUsersRequest;
import com.huaweicloud.sdk.meeting.v1.model.BatchDeleteUsersResponse;
import com.tiens.api.dto.CommonProfitConfigSaveDTO;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.UserRequestDTO;
import com.tiens.api.service.MemberProfitCacheService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.*;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.DubboUserInfoDTO;
import com.tiens.china.circle.api.dubbo.DubboUserAccountService;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.core.HwMeetingUserService;
import com.tiens.meeting.repository.po.*;
import com.tiens.meeting.repository.service.*;
import common.constants.CommonProfitConfigConstants;
import common.enums.MeetingResourceEnum;
import common.exception.ServiceException;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import common.util.cache.CacheKeyUtil;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
public class RpcMeetingUserServiceImpl implements RpcMeetingUserService {

    // @Reference(version = "1.0", mock = "com.tiens.meeting.dubboservice.mock.DubboCommonUserServiceMock")
    @Reference(version = "1.0")
    DubboUserAccountService dubboUserAccountService;

    private final MeetingHostUserDaoService meetingHostUserDaoService;

    private final MeetingLevelResourceConfigDaoService meetingLevelResourceConfigDaoService;

    private final HwMeetingUserService hwMeetingUserService;

    private final HwMeetingCommonService hwMeetingCommonService;

    private final RedissonClient redissonClient;
    private final MeetingBlackUserDaoService meetingBlackUserDaoService;
    private final MeetingMemeberProfitConfigDaoService meetingMemeberProfitConfigDaoService;
    private final MeetingConfig meetingConfig;
    private final MeetingProfitCommonConfigDaoService meetingProfitCommonConfigDaoService;

    private final MemberProfitCacheService memberProfitCacheService;



    /**
     * 通过卓越卡号查询用户
     *
     * @param joyoCode
     * @return
     */
    @Override
    public CommonResult<VMUserVO> queryVMUser(String joyoCode, String accid) {
        if (StringUtils.isAllBlank(joyoCode, accid)) {
            return CommonResult.success(null);
        }
        RBucket<VMUserVO> bucket = null;
        if (StringUtils.isNotBlank(accid)) {
            // 查询缓存
            bucket = redissonClient.getBucket(CacheKeyUtil.getUserInfoKey(accid));
            VMUserVO vmUserCacheVO = bucket.get();
            if (ObjectUtil.isNotNull(vmUserCacheVO)) {
                log.info("缓存命中用户数据，accid:{}", accid);
                return CommonResult.success(vmUserCacheVO);
            }
        }
        if (StringUtils.isNotBlank(joyoCode)) {
            // 查询缓存
            bucket = redissonClient.getBucket(CacheKeyUtil.getUserInfoKey(joyoCode));
            VMUserVO vmUserCacheVO = bucket.get();
            if (ObjectUtil.isNotNull(vmUserCacheVO)) {
                log.info("缓存命中用户数据，joyoCode:{}", joyoCode);
                return CommonResult.success(vmUserCacheVO);
            }
        }

        HomepageBo homepageBo = new HomepageBo();
        homepageBo.setJoyoCode(joyoCode);
        homepageBo.setAccId(accid);
        Result<DubboUserInfoDTO> dtoResult = null;
        try {
            dtoResult = dubboUserAccountService.dubboGetUserInfo(accid, joyoCode);
            log.info("调用VM 查询用户accid：{},joyoCode：{}，返回：{}", accid, joyoCode, JSON.toJSONString(dtoResult));
            if (ObjectUtils.isEmpty(dtoResult.getData())) {
                return CommonResult.success(null);
            }
        } catch (Exception e) {
            log.error("调用VM 查询用户异常,查询用户accid：{},joyoCode：{}", accid, joyoCode, e);
            return CommonResult.success(null);
        }
        DubboUserInfoDTO data = dtoResult.getData();

        VMUserVO vmUserVO = new VMUserVO();
        vmUserVO.setAccid(data.getAccId());
        vmUserVO.setMobile(data.getMobile());
        vmUserVO.setEmail(data.getEmail());
        vmUserVO.setNickName(data.getNickName());
        vmUserVO.setHeadImg(data.getHeadImg());
        vmUserVO.setFansNum(String.valueOf(data.getFansNum()));
        vmUserVO.setLevelCode(data.getLevelCode());
        vmUserVO.setCountry(data.getCountry());
        vmUserVO.setJoyoCode(data.getJoyoCode());

        //todo 设置会员类型
        vmUserVO.setMemberType(data.getMember().equals(0) ? 1 : data.getMemberLevel());
//        vmUserVO.setMemberType(1);
        // 设置缓存
        if (StringUtils.isNotBlank(accid)) {
            bucket.set(vmUserVO);
        }
        if (StringUtils.isNotBlank(joyoCode)) {
            bucket.set(vmUserVO);
        }
        return CommonResult.success(vmUserVO);
    }

    /**
     * 通过卓越卡号添加用户
     *
     * @param joyoCode
     * @param resourceType
     * @return
     */
    @Override
    @Transactional
    public CommonResult addMeetingHostUser(String joyoCode, Integer resourceType) throws ServiceException {
        // 1、查询主持人信息是否存在
        CommonResult<VMUserVO> vmUserVOCommonResult = queryVMUser(joyoCode, "");
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVO)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_HOST_INFO);
        }
        // 校验配置是否合法
        boolean res = checkLevelResource(vmUserVO, resourceType);
        if (!res) {
            return CommonResult.error(GlobalErrorCodeConstants.EXIST_HOST_RESOURCE_CONFIGURATION);
        }
        // 2、添加到主持人表
        MeetingHostUserPO meetingHostUserPO = wrapperMeetingHostUserPO(vmUserVO, resourceType);
        try {
            meetingHostUserDaoService.save(meetingHostUserPO);
        } catch (DuplicateKeyException e) {
            // 重复则修改
            meetingHostUserDaoService.lambdaUpdate().eq(MeetingHostUserPO::getAccId, vmUserVO.getAccid())
                .set(MeetingHostUserPO::getResourceType, resourceType).update();

            // log.error("accId 重复异常");
            // return CommonResult.error(GlobalErrorCodeConstants.EXIST_HOST_INFO);
        }
        // 3、添加主持人信息到华为云用户列表中
        boolean result = hwMeetingUserService.addHwUser(vmUserVO);
        return CommonResult.success(result);
    }

    private boolean checkLevelResource(VMUserVO vmUserVO, Integer resourceType) {
        if (vmUserVO.getLevelCode() == 9) {
            if (!(resourceType >= 7)) {
                // 不符合规则
                return false;
            }
        } else {
            // 1-8级逻辑处理
            MeetingLevelResourceConfigPO configPO = meetingLevelResourceConfigDaoService.lambdaQuery()
                .eq(MeetingLevelResourceConfigPO::getVmUserLevel, vmUserVO.getLevelCode()).oneOpt().get();
            if (resourceType <= configPO.getResourceType()) {
                // 不符合规则
                return false;
            }
        }
        return true;
    }

    /**
     * 通过accid添加会议用户
     *
     * @param accid
     * @return
     */
    @Override
    public CommonResult addMeetingCommonUser(String accid) throws ServiceException {

        RMap<String, String> hwUserFlagMap = redissonClient.getMap(CacheKeyUtil.getHwUserSyncKey());
        String userAddFlag = hwUserFlagMap.get(accid);
        if (StrUtil.isNotBlank(userAddFlag)) {
            return CommonResult.success(userAddFlag);
        }
        // 1、通过accid查询用户
        CommonResult<VMUserVO> vmUserVOCommonResult = queryVMUser("", accid);
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVO)) {
            return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND_HOST_INFO);
        }
        // 2、添加普通信息到华为云用户列表中
        boolean result = false;
        try {
            result = hwMeetingUserService.addHwUser(vmUserVO);
        } catch (Exception e) {
            log.error("添加普通用户发生异常，accid{}！", accid, e);
        }
        return CommonResult.success(result);
    }

    private boolean RemoveHWMeetinguser(List<String> accIds) throws ServiceException {
        BatchDeleteUsersRequest request = new BatchDeleteUsersRequest();

        request.withBody(accIds);
        request.withAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        try {
            MeetingClient meetingClient = hwMeetingCommonService.getMgrMeetingClient();
            BatchDeleteUsersResponse response = meetingClient.batchDeleteUsers(request);
            log.info("华为云删除用户结果：{}", JSON.toJSONString(response));
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            log.error("华为云删除用户业务异常", e);
            throw new ServiceException("1000", e.getErrorMsg());
        }
        return true;
    }

    private MeetingHostUserPO wrapperMeetingHostUserPO(VMUserVO data, Integer resourceType) {
        MeetingHostUserPO meetingHostUserPO = new MeetingHostUserPO();
        meetingHostUserPO.setAccId(data.getAccid());
        meetingHostUserPO.setPhone(data.getMobile());
        meetingHostUserPO.setEmail(data.getEmail());
        meetingHostUserPO.setName(data.getNickName());
        meetingHostUserPO.setJoyoCode(data.getJoyoCode());
        meetingHostUserPO.setLevel(data.getLevelCode());
        meetingHostUserPO.setResourceType(resourceType);
        return meetingHostUserPO;
    }

    /**
     * 移除主持人
     *
     * @param hostUserId
     * @return
     */
    @Override
    public CommonResult removeMeetingHostUser(Long hostUserId) {
        // 1、删除主持人数据库数据
        boolean b = meetingHostUserDaoService.removeById(hostUserId);
        // 2、删除华为云用户数据
        return CommonResult.success(b);
    }

    /**
     * 查询主持人信息
     *
     * @param accId
     * @return
     */
    @Override
    public CommonResult<MeetingHostUserVO> queryMeetingHostUser(String accId) {

        Optional<MeetingHostUserPO> meetingHostUserPOOptional =
            meetingHostUserDaoService.lambdaQuery().eq(MeetingHostUserPO::getAccId, accId).oneOpt();

        if (meetingHostUserPOOptional.isPresent()) {
            MeetingHostUserVO meetingHostUserVO =
                BeanUtil.copyProperties(meetingHostUserPOOptional.get(), MeetingHostUserVO.class);
            return CommonResult.success(meetingHostUserVO);
        }
        return CommonResult.success(null);
    }

    /**
     * 分页查询主持人列表
     *
     * @param pageDTOPageParam
     * @return
     */
    @Override
    public PageResult<MeetingHostUserVO> queryPage(PageParam<MeetingHostPageDTO> pageDTOPageParam) {

        Page<MeetingHostUserPO> page = new Page<>(pageDTOPageParam.getPageNum(), pageDTOPageParam.getPageSize());
        MeetingHostPageDTO condition = pageDTOPageParam.getCondition();

        LambdaQueryWrapper<MeetingHostUserPO> queryWrapper = Wrappers.lambdaQuery(MeetingHostUserPO.class)
            .like(ObjectUtil.isNotEmpty(condition.getName()), MeetingHostUserPO::getName, condition.getName())
            .eq(ObjectUtil.isNotEmpty(condition.getJoyoCode()), MeetingHostUserPO::getJoyoCode, condition.getJoyoCode())
            .like(ObjectUtil.isNotEmpty(condition.getPhone()), MeetingHostUserPO::getPhone, condition.getPhone())
            .like(ObjectUtil.isNotEmpty(condition.getEmail()), MeetingHostUserPO::getEmail, condition.getEmail())
            .orderByDesc(MeetingHostUserPO::getCreateTime);
        Page<MeetingHostUserPO> pagePoResult = meetingHostUserDaoService.page(page, queryWrapper);
        List<MeetingHostUserPO> records = pagePoResult.getRecords();
        List<MeetingHostUserVO> meetingHostUserVOS = BeanUtil.copyToList(records, MeetingHostUserVO.class);
        if (ObjectUtil.isNotEmpty(meetingHostUserVOS)) {
            meetingHostUserVOS.forEach(meetingHostUserVO -> {
                MeetingResourceEnum byCode =
                    MeetingResourceEnum.getByCode(Optional.ofNullable(meetingHostUserVO.getResourceType()).orElse(0));
                meetingHostUserVO.setResourceNum(byCode.getValue());
            });
        }
        PageResult<MeetingHostUserVO> pageResult = new PageResult<>();
        pageResult.setList(meetingHostUserVOS);
        pageResult.setTotal(pagePoResult.getTotal());
        return pageResult;
    }

    @Override
    public CommonResult<List<MeetingResourceTypeVO>> queryResourceTypes(Integer level) {
        QueryWrapper<MeetingLevelResourceConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("resource_type", 0);
        if (level == 9) {
            queryWrapper.eq("vm_user_level", level);
        }
        if (level >= 3 && level != 9) {
            queryWrapper.gt("vm_user_level", level);
        }
        List<MeetingLevelResourceConfigPO> list = meetingLevelResourceConfigDaoService.list(queryWrapper);
        if (!ObjectUtil.isNotEmpty(list)) {
            return CommonResult.success(null);
        }
        ArrayList<MeetingResourceTypeVO> typeVOS = new ArrayList<>();
        list.forEach(meetingLevelResourceConfigPO -> {
            MeetingResourceTypeVO typeVO = new MeetingResourceTypeVO();
            BeanUtil.copyProperties(meetingLevelResourceConfigPO, typeVO);
            MeetingResourceEnum byCode = MeetingResourceEnum.getByCode(typeVO.getResourceType());
            typeVO.setResourceTypeName(byCode.getDesc());
            typeVOS.add(typeVO);
        });
        return CommonResult.success(typeVOS);
    }


    /**
     * 会议黑名单
     *
     * @param
     * @return
     */
    @Override
    public CommonResult<PageResult<MeetingBlackUserVO>> getBlackUserAll(PageParam<MeetingBlackUserVO> bean) {
        Page<MeetingBlackUserPO> meetingApprovePOPage =
                new Page<>(bean.getPageNum(), bean.getPageSize());
        MeetingBlackUserVO condition = bean.getCondition();
        Wrapper<MeetingBlackUserPO> wrapper = Wrappers.lambdaQuery(MeetingBlackUserPO.class)
                .like(StrUtil.isNotBlank(condition.getUserId()), MeetingBlackUserPO::getUserId, condition.getUserId())
                .like(StrUtil.isNotBlank(condition.getNickName()), MeetingBlackUserPO::getNickName, condition.getNickName())
                .like(StrUtil.isNotBlank(condition.getMobile()), MeetingBlackUserPO::getMobile, condition.getMobile())
                .like(StrUtil.isNotBlank(condition.getCountryCode()), MeetingBlackUserPO::getCountryCode, condition.getUserId());

        //查询全部
        Page<MeetingBlackUserPO> page = meetingBlackUserDaoService.page(meetingApprovePOPage, wrapper);

        //使用stream转成vo返回给前端
        List<MeetingBlackUserVO> meetingBlackUserVOList = page.getRecords().stream().map(meetingBlackRecordPO1 -> {
            MeetingBlackUserVO meetingBlackRecordVO = new MeetingBlackUserVO();
            //设置操作人
            BeanUtil.copyProperties(meetingBlackRecordPO1, meetingBlackRecordVO);
            return meetingBlackRecordVO;
        }).collect(Collectors.toList());

        PageResult<MeetingBlackUserVO> meetingpage =  new PageResult<>();
        meetingpage.setList(meetingBlackUserVOList);
        meetingpage.setTotal(page.getTotal());
        return CommonResult.success(meetingpage);
    }

    /**
     * 解除黑名单用户
     * @param userId
     * @return
     */
    @Override
    public CommonResult deleteBlackUser(String userId) {
        meetingBlackUserDaoService.lambdaUpdate().eq(MeetingBlackUserPO::getUserId, userId).remove();
        redissonClient.getBucket(CacheKeyUtil.getBlackUserInfoKey(userId)).delete();
        return CommonResult.success(null);
    }

    /**
     * 批量解除黑名单用户
     * @param userIdList
     * @return
     */
    @Override
    public CommonResult deleteBlackUserAll(List<String> userIdList) {
        meetingBlackUserDaoService.lambdaUpdate().in(MeetingBlackUserPO::getUserId, userIdList).remove();
        userIdList.forEach(userId -> {
            redissonClient.getBucket(CacheKeyUtil.getBlackUserInfoKey(userId)).delete();
        });
        return  CommonResult.success(null);
    }

    /**
     * 添加黑名单用户
     * @param
     * @return
     */
    @Transactional
    @Override
    public CommonResult addBlackUser(String account, UserRequestDTO userRequestDto) {
        List<String> userIdList = userRequestDto.getUserIdList();
        Date endTime = userRequestDto.getEndTime();

        if (endTime.before(new Date())) {
            return CommonResult.errorMsg("结束时间不可以小于当前~");
        }
        List<Boolean> result = new ArrayList<>();
        userIdList.forEach(
                userId -> {
                    // 删除旧的
                    meetingBlackUserDaoService.remove(new LambdaQueryWrapper<MeetingBlackUserPO>().eq(MeetingBlackUserPO::getUserId, userId));

                    // 从缓存中获取被加入黑名单人的信息
                    RBucket<VMUserVO> vorBucket = redissonClient.getBucket(CacheKeyUtil.getUserInfoKey(userId));
                    VMUserVO vmUserVo = vorBucket.get();

                    MeetingBlackUserVO meetingBlackUserVo = new MeetingBlackUserVO();
                    meetingBlackUserVo.setUserId(userId);
                    meetingBlackUserVo.setJoyoCode(vmUserVo == null ? null : vmUserVo.getJoyoCode());

                    // 不知道去哪里取？
                    meetingBlackUserVo.setLastMeetingCode("");
                    meetingBlackUserVo.setMobile(vmUserVo == null ? null : vmUserVo.getMobile());
                    meetingBlackUserVo.setNickName(vmUserVo == null ? null : vmUserVo.getNickName());
                    meetingBlackUserVo.setCountryCode(vmUserVo == null ? null : vmUserVo.getCountry());
                    DateTime startTime = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_DEFAULT);
                    meetingBlackUserVo.setStartTime(startTime);
                    DateTime dateTime = DateUtil.convertTimeZone(endTime, DateUtils.TIME_ZONE_DEFAULT);
                    meetingBlackUserVo.setEndTime(dateTime);
                    meetingBlackUserVo.setOperator(account);

                    // 缓存设置
                    RBucket<MeetingBlackUserVO> bucket = redissonClient.getBucket(CacheKeyUtil.getBlackUserInfoKey(userId));
                    bucket.set(meetingBlackUserVo);
                    // 设置过期时间
                    long differenceInMilliSeconds = endTime.getTime() - startTime.getTime();
                    // 将毫秒转换为秒
                    bucket.expire(differenceInMilliSeconds / 1000, TimeUnit.SECONDS);

                    MeetingBlackUserPO blackUserPo = BeanUtil.copyProperties(meetingBlackUserVo, MeetingBlackUserPO.class);
                    result.add(meetingBlackUserDaoService.save(blackUserPo));
                }
        );
        return CommonResult.success(result);
    }

    /**
     * 会议模版弹窗
     * @return
     */
    @Override
    public CommonResult PopupWindowList(List<LaugeVO> la) {
        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());
        String result = map.get(CommonProfitConfigConstants.CMS_SHOW_FLAG);
        if (StringUtils.isNotBlank(result) && "1".equals(result)) {
            LaugeVO laugeVO = la.get(0);
            LaugeVO laugeVO1 = la.get(1);
            //如果集合为空设置默认值
            if (la.size() == 0){
                la.add(new LaugeVO("en-US", "US", laugeVO.getValue()));
                la.add(new LaugeVO("en-ZN", "ZN", laugeVO1.getValue()));
            }
            redissonClient.getBucket(CacheKeyUtil.getPopupWindowListKeys("countlange")).set(la);
            return CommonResult.success( null);
        }
        if (StringUtils.isNotBlank(result) && "0".equals(result)) {
            //保留原来的数据,不做任何修改
            return CommonResult.success(null);
        }
        return  CommonResult.errorMsg("未成功");
    }

    /**
     * 免费预约限制
     * @param meetingMemeberProfitConfigVOList
     * @return
     */
    @Override
    public CommonResult freeReservationLimit(List<MeetingMemeberProfitConfigVO> meetingMemeberProfitConfigVOList) {
        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());
        String result = map.get(CommonProfitConfigConstants.MEMBER_PROFIT_FLAG);
        if (StringUtils.isNotBlank(result) && "1".equals(result)) {
            //根据传来的数据进行修改表中数据
            if (meetingMemeberProfitConfigVOList != null && !meetingMemeberProfitConfigVOList.isEmpty()) {
                meetingMemeberProfitConfigVOList.forEach(meetingMemeberProfitConfigVO -> {
                    MeetingMemeberProfitConfigPO meetingMemeberProfitConfigPO = BeanUtil.copyProperties(meetingMemeberProfitConfigVO, MeetingMemeberProfitConfigPO.class);
                    //根据membertype修改数据库
                    meetingMemeberProfitConfigDaoService.update(meetingMemeberProfitConfigPO,new LambdaQueryWrapper<MeetingMemeberProfitConfigPO>().eq(MeetingMemeberProfitConfigPO::getMemberType,meetingMemeberProfitConfigVO.getMemberType()));
                    //先将之前的缓存删除
                    redissonClient.getBucket(CacheKeyUtil.getFreeReservationLimitKey(meetingMemeberProfitConfigVO.getMemberType())).delete();
                    //将数据存到redis中
                    redissonClient.getBucket(CacheKeyUtil.getFreeReservationLimitKey(meetingMemeberProfitConfigVO.getMemberType())).set(meetingMemeberProfitConfigVO);
                });
                return CommonResult.success(null);
            }
        }
        return CommonResult.errorMsg("未成功");
    }

    /**
     * 开关接口
     * @param commonProfitConfigSaveDTO
     * @return
     */
    @Override
    public CommonResult opoCommonProfitConfig(CommonProfitConfigSaveDTO commonProfitConfigSaveDTO) {
        RMap<String, String> map = redissonClient.getMap(CacheKeyUtil.getProfitCommonConfigKey());
        if (commonProfitConfigSaveDTO.getMemberProfitFlag()==null){
            map.put(CommonProfitConfigConstants.CMS_SHOW_FLAG,"0");
        }
        if (commonProfitConfigSaveDTO.getCmsShowFlag()==null){
            map.put(CommonProfitConfigConstants.MEMBER_PROFIT_FLAG,"0");
        }
        //redis重新赋值
        map.put(CommonProfitConfigConstants.CMS_SHOW_FLAG,commonProfitConfigSaveDTO.getCmsShowFlag());
        map.put(CommonProfitConfigConstants.MEMBER_PROFIT_FLAG,commonProfitConfigSaveDTO.getMemberProfitFlag());
        //同步修改数据库中的数据
        meetingProfitCommonConfigDaoService.updateById(BeanUtil.copyProperties(commonProfitConfigSaveDTO, MeetingProfitCommonConfigPO.class));
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
     * 回显弹窗内容
     *
     * @return
     */
    @Override
    public CommonResult<List<LaugeVO>> upPopupWindowList() {
        //直接取redis中的数据
        Object countenance = redissonClient.getBucket(CacheKeyUtil.getPopupWindowListKeys("countenance")).get();
        //将数据转成json字符串
        String json = JSON.toJSONString(countenance);
        //再将字符串转成list集合
        List<LaugeVO> objects = JSON.parseArray(json, LaugeVO.class);
        //返回数据
        return CommonResult.success(objects);
    }


    /**
     * 查询会员权益表
     *
     * @return
     */
    @Override
    public CommonResult<List<MeetingMemeberProfitConfigVO>> queryCommonmeberProfitConfig(){
        List<MeetingMemeberProfitConfigPO> list = meetingMemeberProfitConfigDaoService.list();
       return CommonResult.success(BeanUtil.copyToList(list, MeetingMemeberProfitConfigVO.class));
    }

}
