package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.util.RandomUtil;
import com.huaweicloud.sdk.meeting.v1.utils.HmacSHA256;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MeetingRoomServiceImpl implements RpcMeetingRoomService {

    @Autowired
    MeetingConfig meetingConfig;

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
        String authorization = "HMAC-SHA256 signature=" + HmacSHA256.encode(data, meetingConfig.getAppKey());
        VMMeetingCredentialVO vmMeetingCredentialVO = new VMMeetingCredentialVO();
        vmMeetingCredentialVO.setSignature(authorization);
        vmMeetingCredentialVO.setExpireTime(expireTime);
        vmMeetingCredentialVO.setNonce(nonce);
        vmMeetingCredentialVO.setUserId(userId);
        return CommonResult.success(vmMeetingCredentialVO);
    }
}
