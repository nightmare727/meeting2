package com.tiens.meeting.dubboservice.impl;

import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.service.MemberProfitService;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

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

    /**
     * 查询首页头图展示
     *
     * @return
     */
    @Override
    public CommonResult getCmsShow() {
        return CommonResult.success(meetingConfig.getCmsShowConfig());
    }

    /**
     * 推送订单
     *
     * @param pushOrderDTO
     * @return
     */
    @Override
    public CommonResult pushOrder(PushOrderDTO pushOrderDTO) {
        return CommonResult.success(null);
    }
}
