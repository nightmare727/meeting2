package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.VMUserVO;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RestController
@Slf4j
@Tag(name = "会议主持人相关接口")
@RequestMapping("/mtuser")
public class MeetingUserController {

    @Reference
    RpcMeetingRoomService rpcMeetingRoomService;

    @Reference
    RpcMeetingUserService rpcMeetingUserService;

    /**
     * 查询Vmoment用户信息
     *
     * @param joyoCode
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryVMUser/{joyoCode}")
    public CommonResult<VMUserVO> queryMeetingHostUser(@PathVariable("joyoCode") String joyoCode) throws Exception {
        CommonResult<VMUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryVMUser(joyoCode,"");
        return vmUserVOCommonResult;
    }

    /**
     * 添加会议主持人
     *
     * @param joyoCode
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/addMeetingHostUser")
    public CommonResult addMeetingHostUser(@RequestParam("joyoCode") String joyoCode) throws Exception {
        CommonResult commonResult = rpcMeetingUserService.addMeetingHostUser(joyoCode);
        return commonResult;
    }

    /**
     * 移除会议主持人
     *
     * @param hostUserId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @DeleteMapping("/removeMeetingHostUser/{hostUserId}")
    public CommonResult addMeetingHostUser(@PathVariable("hostUserId") Long hostUserId) throws Exception {
        CommonResult commonResult = rpcMeetingUserService.removeMeetingHostUser(hostUserId);
        return commonResult;
    }

    @ResponseBody
    @PostMapping("/queryPage")
    public CommonResult<PageResult<MeetingHostUserVO>> queryPage(
        @RequestBody PageParam<MeetingHostPageDTO> pageDTOPageParam) throws Exception {
        PageResult<MeetingHostUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryPage(pageDTOPageParam);
        return CommonResult.success(vmUserVOCommonResult);
    }

}
