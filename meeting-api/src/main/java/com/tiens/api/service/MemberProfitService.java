package com.tiens.api.service;

import com.tiens.api.dto.*;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;

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
    CommonResult<MeetingBlackUserVO> getBlackUser(String finalUserId);

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
     * 会议黑名单
     *
     * @param
     * @param bean
     * @return
     */
    CommonResult<PageResult<MeetingBlackUserVO>> getBlackUserAll(PageParam<MeetingBlackUserVO> bean);

    /**
     * 解除黑名单用户
     * @param userId
     * @return
     */
    CommonResult deleteBlackUser(String userId);

    /**
     * 批量解除黑名单用户
     * @param userIdList
     * @return
     */
    CommonResult deleteBlackUserAll(List<String> userIdList);

    /**
     * 添加黑名单用户
     * @param meetingBlackRecordVO
     * @return
     */
    CommonResult addBlackUser(MeetingBlackUserVO meetingBlackRecordVO);

    /**
     * 会议模版弹窗
     * @return
     */
    CommonResult PopupWindowList(Long meetingRoomId,String text,String nation_code);

    /**
     * 免费预约限制
     * @param meetingMemeberProfitConfigVOList
     * @return
     */
    CommonResult freeReservationLimit(List<MeetingMemeberProfitConfigVO> meetingMemeberProfitConfigVOList);

    /**
     * 开关接口
     * @param commonProfitConfigSaveDTO
     * @return
     */
    CommonResult opoCommonProfitConfig(CommonProfitConfigSaveDTO commonProfitConfigSaveDTO);

    /**
     * 获取房间权益价格配置
     *
     */
    CommonResult<List<MeetingPaidSettingVO>> getMeetingPaidSettingList();

    /**
     * 修改房间权益价格配置
     *
     */
    CommonResult updMeetingPaidSetting(MeetingPaidSettingVO request);
}
