package com.tiens.meeting.web.controller;

import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.service.MemberProfitService;
import common.pojo.CommonResult;
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

    @ResponseBody
    @GetMapping("/getCmsShow")
    public CommonResult getCmsShow() throws Exception {

        return memberProfitService.getCmsShow();
    }
}
