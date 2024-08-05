package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.CommonProfitConfigSaveDTO;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.vo.CommonProfitConfigQueryVO;
import com.tiens.api.vo.UserMemberProfitEntity;
import common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询权益公共配置
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryCommonProfitConfig")
    public CommonResult<CommonProfitConfigQueryVO> queryCommonProfitConfig() throws Exception {
        CommonResult<CommonProfitConfigQueryVO> result = memberProfitService.queryCommonProfitConfig();
        return result;
    }

    /**
     * 保存通用权益配置
     *
     * @param commonProfitConfigSaveDTO
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/saveCommonProfitConfig")
    public CommonResult saveCommonProfitConfig(@RequestBody CommonProfitConfigSaveDTO commonProfitConfigSaveDTO)
        throws Exception {
        CommonResult result = memberProfitService.saveCommonProfitConfig(commonProfitConfigSaveDTO);
        return result;
    }

    /**
     * 会议付费设置
     *
     */
    @ResponseBody
    @GetMapping("closeMeeting")
    public CommonResult getMeetingPaidSettingList() {
        return memberProfitService.getMeetingPaidSettingList();
    }

}
