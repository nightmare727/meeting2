package com.tiens.meeting.web.controller;

import com.tiens.api.dto.BuyMeetingProfitDTO;
import com.tiens.api.dto.CmsShowGetDTO;
import com.tiens.api.dto.MeetingProfitPurchaseDetailGetDTO;
import com.tiens.api.dto.ProfitPaidCheckOutGetDTO;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/3
 * @Version 1.0
 * @Company: tiens
 */
@RestController
@Slf4j
@RequestMapping(value = "/profit")
@Tag(name = "权益相关接口")
public class MemberProfitController {

    @Reference
    MemberProfitService memberProfitService;

    /**
     * 推送订单
     *
     * @param pushOrderDTO
     * @return
     * @throws Exception
     */
   /* @ResponseBody
    @PostMapping("/pushOrder")
    public CommonResult pushOrder(@RequestBody PushOrderDTO pushOrderDTO) throws Exception {

        return memberProfitService.pushOrder(pushOrderDTO);
    }*/

    /**
     * 获取首页CMS图片展示
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/getCmsShow")
    public CommonResult<CmsShowVO> getCmsShow(@RequestHeader("nation_id") String nationId,
        @RequestHeader(value = "language_id", defaultValue = "zh-CN") String languageId,
        @RequestBody CmsShowGetDTO cmsShowGetDTO) throws Exception {
        cmsShowGetDTO.setNationId(nationId);
        cmsShowGetDTO.setLanguageId(languageId);
        return memberProfitService.getCmsShow(cmsShowGetDTO);
    }

    /**
     * 查询用户黑名单信息
     *
     * @param finalUserId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/isBlackUser")
    public CommonResult<MeetingBlackUserVO> isBlackUser(@RequestHeader("finalUserId") String finalUserId) {
        return memberProfitService.getBlackUser(finalUserId);
    }

    /**
     * 查询用户权益
     *
     * @param finalUserId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/getUserProfit")
    public CommonResult<MeetingUserProfitVO> getUserProfit(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("memberType") Integer memberType) throws Exception {

        return memberProfitService.getUserProfit(finalUserId, memberType);
    }

    /**
     * 查询全部权益
     *
     * @param
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/getALlProfit")
    public CommonResult<List<UserMemberProfitEntity>> getALlProfit() throws Exception {
        return memberProfitService.getALlProfit();
    }

    /**
     * 查询权益商品列表
     *
     * @param
     * @return
     */
    @ResponseBody
    @PostMapping("/queryMeetingProfitProductList")
    public CommonResult<List<MeetingProfitProductListVO>> queryMeetingProfitProductList() {
        return memberProfitService.queryMeetingProfitProductList();
    }

    /**
     * 购买权益
     *
     * @param
     * @return
     */
    @ResponseBody
    @PostMapping("/buyMeetingProfit")
    public CommonResult buyMeetingProfit(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("joyoCode") String joyoCode, @RequestHeader("nation_id") String nationId,
        @RequestBody BuyMeetingProfitDTO buyMeetingProfitDTO) {
        buyMeetingProfitDTO.setFinalUserId(finalUserId);
        buyMeetingProfitDTO.setJoyoCode(joyoCode);
        buyMeetingProfitDTO.setNationId(nationId);

        return memberProfitService.buyMeetingProfit(buyMeetingProfitDTO);
    }

    /**
     * 查询权益购买详情
     *
     * @param finalUserId
     * @param memberType
     * @param meetingProfitPurchaseDetailGetDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/getMeetingProfitPurchaseDetail")
    public CommonResult<MeetingProfitPurchaseDetailVO> getMeetingProfitPurchaseDetail(
        @RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("memberType") Integer memberType,
        @RequestBody MeetingProfitPurchaseDetailGetDTO meetingProfitPurchaseDetailGetDTO) {
        meetingProfitPurchaseDetailGetDTO.setFinalUserId(finalUserId);
        meetingProfitPurchaseDetailGetDTO.setMemberType(memberType);
        return memberProfitService.getMeetingProfitPurchaseDetail(meetingProfitPurchaseDetailGetDTO);
    }

    /**
     * 查询结算金额
     *
     * @param finalUserId
     * @param memberType
     * @param profitPaidCheckOutGetDto
     * @return
     */
    @ResponseBody
    @PostMapping("/getProfitPaidCheckOut")
    public CommonResult<ProfitPaidCheckOutGetVO> getProfitPaidCheckOut(
        @RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("memberType") Integer memberType,
        @RequestBody ProfitPaidCheckOutGetDTO profitPaidCheckOutGetDto) {
        return memberProfitService.getProfitPaidCheckOut(profitPaidCheckOutGetDto);
    }

}
