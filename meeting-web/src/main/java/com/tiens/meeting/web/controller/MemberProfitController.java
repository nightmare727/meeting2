package com.tiens.meeting.web.controller;

import com.tiens.api.dto.*;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CommonResult<MeetingBlackUserVO> isBlackUser(@RequestHeader("finalUserId") String finalUserId)
        throws Exception {

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
     * 查询用户黑名单信息
     *
     * @param bean
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/selectBlackUser")
    public CommonResult<List<MeetingBlackRecordVO>> selectBlackUser(@RequestBody PageParam<MeetingBlackRecordVO> bean)
            throws Exception {
        CommonResult<PageResult<MeetingBlackRecordVO>> pageResult = memberProfitService.getBlackUserAll(bean);
        return CommonResult.success(pageResult.getData().getList());
    }

    /**
     * 根据用户id删除
     */
    @ResponseBody
    @GetMapping("/deleteBlackUser")
    public CommonResult deleteBlackUser(@RequestHeader("finalUserId") String finalUserId,@RequestParam("userId") String userId)
            throws Exception {
        return memberProfitService.deleteBlackUser(userId);
    }


    /**
     * 批量解除黑名单
     */
    @ResponseBody
    @PostMapping("/deleteBlackUserAll")
    public CommonResult deleteBlackUserAll(@RequestHeader("finalUserId") String finalUserId,@RequestBody List<String> userIdList)
            throws Exception {
        return memberProfitService.deleteBlackUserAll(userIdList);
    }

    /**
     * 添加黑名单
     */
    @ResponseBody
    @PostMapping("/addBlackUser")
    public CommonResult addBlackUser(@RequestBody MeetingBlackRecordVO meetingBlackRecordVO)
            throws Exception {
        return memberProfitService.addBlackUser(meetingBlackRecordVO);
    }

    /**
     * 会议板块弹窗
     */
    @ResponseBody
    @PostMapping("/popupWindow")
    public CommonResult checkProfit(Long meetingRoomId,String text,String nation_code)
            throws Exception {
        return memberProfitService.PopupWindowList(meetingRoomId,text,nation_code);
    }

    /**
     * 会议免费预约限制
     */
    @ResponseBody
    @PostMapping("/freeReservationLimit")
    public CommonResult freeReservationLimit(List<MeetingMemeberProfitConfigVO> meetingMemeberProfitConfigVOList)
            throws Exception {
        return memberProfitService.freeReservationLimit(meetingMemeberProfitConfigVOList);
    }


    /**
     * 开关接口
     */
    @ResponseBody
    @PostMapping("/opoCommonProfitConfig")
    public CommonResult opoCommonProfitConfig(@RequestBody CommonProfitConfigSaveDTO commonProfitConfigSaveDTO)
            throws Exception {
        return memberProfitService.opoCommonProfitConfig(commonProfitConfigSaveDTO);
    }


}
