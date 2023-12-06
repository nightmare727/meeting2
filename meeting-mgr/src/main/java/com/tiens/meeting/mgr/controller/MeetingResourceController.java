package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.dto.MeetingResoucePageDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResouceVO;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
import com.tiens.api.vo.VMUserVO;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/22
 * @Version 1.0
 * @Company: tiens
 */
@RestController
@Slf4j
@Tag(name = "会议资源相关接口")
@RequestMapping("/res")
public class MeetingResourceController {

    @Reference
    RPCMeetingTimeZoneService rpcMeetingTimeZoneService;
    @Reference
    RPCMeetingResourceService rpcMeetingResourceService;

    /**
     * 获取时区列表
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/queryTimeZoneConfig")
    public CommonResult<List<MeetingTimeZoneConfigVO>> getTimeZoneList() {
        return rpcMeetingTimeZoneService.getList();
    }

    /**
     * 获取华为云会议资源并列表展示
     *
     * @return
     */
    @ResponseBody
    @PostMapping("/queryMeetingResoucePage")
    public CommonResult queryMeetingResoucePage(
            @RequestBody PageParam<MeetingResoucePageDTO> pageDTOPageParam) throws Exception {
        rpcMeetingResourceService.SearchCorpVmrSolution1();
        rpcMeetingResourceService.SearchCorpVmrSolution2();
        PageResult<MeetingResouceVO> vmUserVOCommonResult = rpcMeetingResourceService.queryMeetingResoucePage(pageDTOPageParam);
        return CommonResult.success(vmUserVOCommonResult);
    }




    /**
     * 更改会议资源状态:取消分配,操作后，此资源变为公有。
     *
     * @param vmrId
     * @return
     */
    @ResponseBody
    @PostMapping("/updateMeetingStatus")
    public CommonResult updateMeetingStatus(String vmrId) throws Exception{
        CommonResult commonResult = rpcMeetingResourceService.updateMeetingStatus(vmrId);
        return commonResult;
    };




    /**
     * 公有空闲状态 即 公有资源 无人预约时
     *  可进行 分配操作,分配后
     *  此资源变为私有状态
     *
     * @param joyoCode
     * @return
     */
    @ResponseBody
    @PostMapping("/assignMeetingResouce")
    public CommonResult assignMeetingResouce(String joyoCode) throws Exception{
        CommonResult commonResult = rpcMeetingResourceService.assignMeetingResouce(joyoCode);
        return commonResult;
    };

    /**
     * 查询用户ID
     *
     * @param joyoCode
     * @return
     */
    @ResponseBody
    @PostMapping("/selectUserByJoyoCode")
    public MeetingHostUserVO selectUserByJoyoCode(String joyoCode) throws Exception {
        MeetingHostUserVO meetingHostUserVO = rpcMeetingResourceService.selectUserByJoyoCode(joyoCode);
        if (meetingHostUserVO != null){
            return meetingHostUserVO;
        }else {
            return null;
        }
    }



    /**
     * 公有预约 即为公有资源 可设为公有空闲
     * 功能待定!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * @param
     * @return
     */
    public CommonResult updateMeetingResouce(String joyoCode) throws Exception{


        return null;
    }
}
