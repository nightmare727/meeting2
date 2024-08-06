package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.CommonProfitConfigSaveDTO;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.UserRequestDTO;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

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
    public CommonResult addMeetingHostUser(@RequestParam("joyoCode") String joyoCode,@RequestParam("resource")Integer resource
    ) throws Exception {
        CommonResult commonResult = rpcMeetingUserService.addMeetingHostUser(joyoCode,resource);
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

    /**
     * 主持人列表查询
     * @param pageDTOPageParam
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/queryPage")
    public CommonResult<PageResult<MeetingHostUserVO>> queryPage(
        @RequestBody PageParam<MeetingHostPageDTO> pageDTOPageParam) throws Exception {
        PageResult<MeetingHostUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryPage(pageDTOPageParam);
        return CommonResult.success(vmUserVOCommonResult);
    }

    /**
     * 查询会议资源配置列表
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryResourceTypes")
    public CommonResult<List<MeetingResourceTypeVO>> queryResourceTypes(@RequestParam("level") Integer level){
        CommonResult<List<MeetingResourceTypeVO>> listCommonResult = rpcMeetingUserService.queryResourceTypes(level);
        return listCommonResult;
    }


    /**
     * 查询会议黑名单信息
     *
     * @param bean
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/selectMeetingBlack")
    public CommonResult<PageResult<MeetingBlackUserVO>> selectBlackUser(@RequestHeader("finalUserId") String finalUserId,@RequestBody PageParam<MeetingBlackUserVO> bean)
            throws Exception {

        return rpcMeetingUserService.getBlackUserAll(finalUserId,bean);
    }

    /**
     * 根据用户id删除
     */
    @ResponseBody
    @GetMapping("/deleteMeetingBlack")
    public CommonResult deleteBlackUser(@RequestParam("userId") String userId)
            throws Exception {
        return rpcMeetingUserService.deleteBlackUser(userId);
    }


    /**
     * 批量解除黑名单
     */
    @ResponseBody
    @PostMapping("/deleteMeetingBlackAll")
    public CommonResult deleteBlackUserAll(@RequestBody List<String> userIdList)
            throws Exception {
        return rpcMeetingUserService.deleteBlackUserAll(userIdList);
    }

    /**
     * 添加黑名单
     */
    @ResponseBody
    @PostMapping("/addBlackMeeting")
    public CommonResult addBlackUser(@RequestBody UserRequestDTO userRequestDTO)
            throws Exception {
        return rpcMeetingUserService.addBlackUser(userRequestDTO);
    }

    /**
     * 会议板块弹窗
     */
    @ResponseBody
    @PostMapping("/popupWindow")
    public CommonResult checkProfit(@RequestBody List<LaugeVO> la)
            throws Exception {
        return rpcMeetingUserService.PopupWindowList(la);
    }


    /**
     * 会议板块弹窗回显
     */
    @ResponseBody
    @PostMapping("/upPopupWindowList")
    public CommonResult<LaugeVO> getUserProfitConfig(@RequestBody List<LaugeVO> la)
            throws Exception {
        return rpcMeetingUserService.upPopupWindowList(la);
    }

    /**
     * 会议免费预约限制
     */
    @ResponseBody
    @PostMapping("/freeReservationLimit")
    public CommonResult freeReservationLimit(@RequestBody List<MeetingMemeberProfitConfigVO> meetingMemeberProfitConfigVOList)
            throws Exception {
        return rpcMeetingUserService.freeReservationLimit(meetingMemeberProfitConfigVOList);
    }


    /**
     * 开关接口
     */
    @ResponseBody
    @PostMapping("/opoCommonProfitConfig")
    public CommonResult opoCommonProfitConfig(@RequestBody CommonProfitConfigSaveDTO commonProfitConfigSaveDTO)
            throws Exception {
        return rpcMeetingUserService.opoCommonProfitConfig(commonProfitConfigSaveDTO);
    }


}
