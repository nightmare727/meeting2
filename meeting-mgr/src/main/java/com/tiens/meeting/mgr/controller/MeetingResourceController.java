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
     * 获取会议资源列表
     *
     * @return
     */
    @ResponseBody
    @PostMapping("/queryMeetingResoucePage")
    public CommonResult<PageResult<MeetingResouceVO>> queryMeetingResoucePage(
            @RequestBody PageParam<MeetingResoucePageDTO> pageDTOPageParam) throws Exception {
        PageResult<MeetingResouceVO> vmUserVOCommonResult = rpcMeetingResourceService.queryMeetingResoucePage(pageDTOPageParam);
        return CommonResult.success(vmUserVOCommonResult);
    }

    /**
     * 更改会议资源状态:置为空闲
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
     * 分配会议资源
     *
     * @param meetingResouceIdDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/assignMeetingResouce")
    public CommonResult assignMeetingResouce(MeetingResouceIdDTO meetingResouceIdDTO) throws Exception{
        CommonResult commonResult = rpcMeetingResourceService.assignMeetingResouce(meetingResouceIdDTO);
        return commonResult;
    };

    /**
     * 查询主持人
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

}
