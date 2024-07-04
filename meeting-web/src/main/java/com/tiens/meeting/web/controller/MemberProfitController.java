package com.tiens.meeting.web.controller;

import com.tiens.api.dto.CmsShowGetDTO;
import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.CmsShowVO;
import com.tiens.api.vo.MeetingBlackUserVO;
import com.tiens.api.vo.MeetingUserProfitVO;
import common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

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
    @ResponseBody
    @PostMapping("/pushOrder")
    public CommonResult pushOrder(@RequestBody PushOrderDTO pushOrderDTO) throws Exception {

        return memberProfitService.pushOrder(pushOrderDTO);
    }

    /**
     * 获取首页CMS图片展示
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/getCmsShow")
    public CommonResult<CmsShowVO> getCmsShow(@RequestBody CmsShowGetDTO cmsShowGetDTO) throws Exception {
        return memberProfitService.getCmsShow();
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
    public CommonResult<MeetingUserProfitVO> getUserProfit(@RequestHeader("finalUserId") String finalUserId)
        throws Exception {

        return memberProfitService.getUserProfit(finalUserId);
    }

}
