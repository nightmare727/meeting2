package com.tiens.api.service;

import com.tiens.api.dto.*;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;

import java.util.List;

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
     * 保存权益记录
     *
     * @param meetingRoomContextDTO
     * @return
     */
    CommonResult saveUserProfitRecord(MeetingRoomContextDTO meetingRoomContextDTO, Long meetingId);

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
    CommonResult<MeetingBlackUserVO> getBlackUser(String finalUserId, String joyoCode);

    /**
     * 查询用户权益
     *
     * @param finalUserId
     * @param memberType
     * @return
     */
    CommonResult<MeetingUserProfitVO> getUserProfit(String finalUserId, Integer memberType);

    /**
     * 查询权益配置
     *
     * @return
     */
    CommonResult<List<UserMemberProfitEntity>> queryUserProfitConfig();

    /**
     * 修改会员权益
     *
     * @param userMemberProfitModifyEntity
     * @return
     */
    CommonResult modUserMemberProfit(UserMemberProfitModifyEntity userMemberProfitModifyEntity);

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
    CommonResult settleMemberProfit(Long meetingId, String imUserId, String resourceType, String languageId,
        long betweenMinutes);

    /**
     * 保存通用权益配置
     *
     * @param commonProfitConfigSaveDTO
     * @return
     */
    CommonResult saveCommonProfitConfig(CommonProfitConfigSaveDTO commonProfitConfigSaveDTO);

    /**
     * 查询权益公共配置
     *
     * @return
     */
    CommonResult<CommonProfitConfigQueryVO> queryCommonProfitConfig();

    /**
     * 查询权益商品列表
     *
     * @return
     */
    CommonResult<List<MeetingProfitProductListVO>> queryMeetingProfitProductList();

    /**
     * 购买权益
     *
     * @param buyMeetingProfitDTO
     * @return
     */
    CommonResult buyMeetingProfit(BuyMeetingProfitDTO buyMeetingProfitDTO);

    /**
     * 获取房间权益价格配置
     */
    CommonResult<List<MeetingPaidSettingVO>> getMeetingPaidSettingList();

    /**
     * 修改房间权益价格配置
     */
    CommonResult updMeetingPaidSetting(MeetingPaidSettingVO request);

    /**
     * 查询全部权益配置
     *
     * @return
     */
    CommonResult<List<UserMemberProfitEntity>> getALlProfit();

    /**
     * 查询会员权益购买详情
     *
     * @param meetingProfitPurchaseDetailGetDTO
     * @return
     */
    CommonResult<MeetingProfitPurchaseDetailVO> getMeetingProfitPurchaseDetail(MeetingProfitPurchaseDetailGetDTO meetingProfitPurchaseDetailGetDTO);

    /**
     * 查询结算金额
     *
     * @param profitPaidCheckOutGetDTO
     * @return
     */
    CommonResult<ProfitPaidCheckOutGetVO> getProfitPaidCheckOut(ProfitPaidCheckOutGetDTO profitPaidCheckOutGetDTO);
}
