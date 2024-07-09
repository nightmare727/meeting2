package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.api.service.MeetingCacheService;
import com.tiens.api.vo.VMUserVO;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.DubboUserInfoDTO;
import com.tiens.china.circle.api.dubbo.DubboUserAccountService;
import common.pojo.CommonResult;
import common.util.cache.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MeetingCacheServiceImpl implements MeetingCacheService {

    @Autowired
    RedissonClient redissonClient;

    @Reference(version = "1.0")
    DubboUserAccountService dubboUserAccountService;

    /**
     * 根据会议id刷新缓存
     *
     * @param meetingRoomIdList
     * @return
     */
    @Override
    public CommonResult refreshMeetingRoomCache(List<Long> meetingRoomIdList) {
        return null;
    }

    /**
     * 根据会议id删除缓存
     *
     * @param meetingRoomIdList
     * @return
     */
    @Override
    public CommonResult deleteMeetingRoomCache(List<Long> meetingRoomIdList) {
        return null;
    }

    /**
     * 修改用户缓存
     *
     * @param imUserId
     * @param joyoCode
     * @return
     */
    @Override
    public CommonResult<DubboUserInfoDTO> refreshMeetingUserCache(String imUserId, String joyoCode) {
        log.info("修改用户缓存入参imUserId：{},joyoCode：{}", imUserId, joyoCode);
        Result<DubboUserInfoDTO> dubboUserInfoDTOResult = dubboUserAccountService.dubboGetUserInfo(imUserId, joyoCode);
        log.info("用户ES数据返回：{}", JSON.toJSONString(dubboUserInfoDTOResult));

        DubboUserInfoDTO data = dubboUserInfoDTOResult.getData();

        if (ObjectUtil.isEmpty(data)) {
            log.error("用户修改-查无此用户！,userId：{}", imUserId);
            return CommonResult.errorMsg("用户修改-查无此用户");
        }

        RBucket<VMUserVO> ImUserIdCache = redissonClient.getBucket(CacheKeyUtil.getUserInfoKey(imUserId));
        RBucket<VMUserVO> joyoCodeCache = redissonClient.getBucket(CacheKeyUtil.getUserInfoKey(data.getJoyoCode()));
        //修改用户缓存

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

        // 设置缓存
        ImUserIdCache.set(vmUserVO);
        joyoCodeCache.set(vmUserVO);

        return CommonResult.success(data);
    }
}
