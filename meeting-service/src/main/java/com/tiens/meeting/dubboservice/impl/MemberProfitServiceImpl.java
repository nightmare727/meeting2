package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.MeetingBlackUserVO;
import com.tiens.api.vo.MeetingUserProfitVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.MeetingBlackUserPO;
import com.tiens.meeting.repository.service.MeetingBlackUserDaoService;
import common.pojo.CommonResult;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;

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

    private final MeetingBlackUserDaoService meetingBlackUserDaoService;

    /**
     * 校验用户权益
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public CommonResult checkProfit(MeetingRoomContextDTO meetingRoomContextDTO) {
        return null;
    }

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
        List<MeetingBlackUserPO> blackUserPOList = meetingBlackUserDaoService.lambdaQuery()
            .eq(MeetingBlackUserPO::getUserId, finalUserId)
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
     * @return
     */
    @Override
    public CommonResult<MeetingUserProfitVO> getUserProfit(String finalUserId) {

        return null;
    }
}
