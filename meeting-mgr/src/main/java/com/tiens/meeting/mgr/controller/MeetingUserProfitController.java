package com.tiens.meeting.mgr.controller;

import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.UserMemberProfitEntity;
import com.tiens.api.vo.VMUserVO;
import common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RestController
@Slf4j
@Tag(name = "会员权益相关接口")
@RequestMapping("/profit")
public class MeetingUserProfitController {

    @Reference
    MemberProfitService memberProfitService;

    /**
     * 查询Vmoment用户权益
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryUserProfitConfig")
    public CommonResult<List<UserMemberProfitEntity>> queryUserProfitConfig() throws Exception {
        CommonResult<List<UserMemberProfitEntity>> result = memberProfitService.queryUserProfitConfig();
        return result;
    }

}
