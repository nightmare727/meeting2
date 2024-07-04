package com.tiens.api.service;

import com.tiens.api.dto.CmsShowGetDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.vo.CmsShowVO;
import com.tiens.api.vo.MeetingBlackUserVO;
import com.tiens.api.vo.MeetingUserProfitVO;
import common.pojo.CommonResult;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
public interface MemberProfitService {
    /**
     * 校验用户权益
     *
     * @param meetingRoomContextDTO
     * @return
     */
    CommonResult checkProfit(MeetingRoomContextDTO meetingRoomContextDTO);

    /**
     * 查询首页头图展示
     *
     * @return
     */
    CommonResult<CmsShowVO> getCmsShow(CmsShowGetDTO cmsShowGetDTO);

    /**
     * 推送订单
     *
     * @param pushOrderDTO
     * @return
     */
    CommonResult pushOrder(PushOrderDTO pushOrderDTO);

    /**
     * 查询黑名单用户
     *
     * @param finalUserId
     * @return
     */
    CommonResult<MeetingBlackUserVO> getBlackUser(String finalUserId);

    /**
     * 查询用户权益
     *
     * @param finalUserId
     * @return
     */
    CommonResult<MeetingUserProfitVO> getUserProfit(String finalUserId);
}
