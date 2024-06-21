package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.core.HwMeetingUserService;
import common.enums.VmUserSourceEnum;
import common.exception.ServiceException;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 */
@Service
@Slf4j
public class HwMeetingUserServiceImpl implements HwMeetingUserService {

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Autowired
    RedissonClient redissonClient;

    private String buildHWName(VMUserVO vmUserVO) {
        String specificSymbol = "&<>/()' \"";
        String brief = StrUtil.brief(vmUserVO.getNickName(), 64);

        if (StrUtil.containsAny(brief, specificSymbol.toCharArray())) {
            //包含特殊符号，重写昵称
            brief = vmUserVO.getJoyoCode();
        }
        return brief;
    }

    private String buildHWAccount(VMUserVO vmUserVO) {
        String source = VmUserSourceEnum.getNameByCode(vmUserVO.getSource());
        String joyoCode = ObjectUtil.defaultIfBlank(vmUserVO.getJoyoCode(), "DEFAULT" + RandomUtil.randomNumbers(20));
        return source + "-" + joyoCode;
    }

    /**
     * 创建用户
     *
     * @param vmUserVO
     * @return
     */
    @Override
    public Boolean addHwUser(VMUserVO vmUserVO) {
        AddUserRequest request = new AddUserRequest();
        AddUserDTO body = new AddUserDTO();
        body.withName(buildHWName(vmUserVO));
        body.withThirdAccount(vmUserVO.getAccid());
        //华为账号为卓越卡号拼接
        body.withAccount(buildHWAccount(vmUserVO));
        request.withBody(body);
        RMap<String, String> hwUserFlagMap = redissonClient.getMap(CacheKeyUtil.getHwUserSyncKey());

        //userId
        MeetingClient mgrMeetingClient = hwMeetingCommonService.getMgrMeetingClient();
        try {
            AddUserResponse response = mgrMeetingClient.addUser(request);
            hwUserFlagMap.fastPut(vmUserVO.getAccid(), "ok");
            log.info("华为云添加用户结果：{}", JSON.toJSONString(response));
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            if ("USG.201040001".equals(e.getErrorCode())) {
                //账号已经存在
//                log.error("账号已存在，无需添加,账号：{},异常：{} ", vmUserVO.getAccid(), e);
                //更新用户信息
                this.modHwUser(vmUserVO);

                hwUserFlagMap.fastPut(vmUserVO.getAccid(), "ok");
                return true;
            }
            log.error("华为云添加用户业务异常", e);
            throw new ServiceException("1000", e.getErrorMsg());
        }
        return true;

    }

    /**
     * 修改用户
     *
     * @param vmUserVO
     */
    @Override
    public Boolean modHwUser(VMUserVO vmUserVO) {
        //尝试修改华为云用户信息
        MeetingClient meetingClient = hwMeetingCommonService.getMgrMeetingClient();
        UpdateUserRequest request = new UpdateUserRequest();
        ModUserDTO body = new ModUserDTO();
        //1-买买 2-云购 3 Vshare 4 瑞狮 5意涵永
        body.withName(StrUtil.brief(vmUserVO.getNickName(), 64));
        request.withBody(body);
        request.withAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        request.withAccount(vmUserVO.getAccid());
        try {
            UpdateUserResponse updateUserResponse = meetingClient.updateUser(request);
            log.info("修改华为云用户结果：{}", updateUserResponse);
        } catch (Exception e) {
            log.info("修改华为云用户异常", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
