package com.tiens.meeting.web.controller;

import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.VMMeetingCredentialVO;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RequestMapping(value = "/mtuser")
@Slf4j
@RestController
public class MeetingUserController {

    @Reference
    RpcMeetingRoomService rpcMeetingRoomService;

    @Reference
    RpcMeetingUserService rpcMeetingUserService;

    /**
     * 查询主持人信息
     *
     * @param accid
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryMeetingHostUser/{accid}")
    public CommonResult<MeetingHostUserVO> queryMeetingHostUser(@PathVariable("accid") String accid) throws Exception {
        CommonResult<MeetingHostUserVO> meetingHostUserVOCommonResult =
            rpcMeetingUserService.queryMeetingHostUser(accid);
        return meetingHostUserVOCommonResult;
    }

    /**
     * 查询登录认证
     *
     * @param accid
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/getCredential/{accid}")
    public CommonResult<VMMeetingCredentialVO> getCredential(@PathVariable("accid") String accid) throws Exception {
        return rpcMeetingRoomService.getCredential(accid);
    }
}
