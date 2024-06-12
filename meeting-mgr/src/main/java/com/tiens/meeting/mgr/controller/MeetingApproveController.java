package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.MeetingApproveOperateDTO;
import com.tiens.api.dto.MeetingApprovePageDTO;
import com.tiens.api.service.MeetingApproveService;
import com.tiens.api.vo.MeetingApproveVO;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 蔚文杰
 * @Date: 2024/6/12
 * @Version 1.0
 * @Company: tiens
 */

@RestController
@Slf4j
@RequestMapping(value = "/approve")
public class MeetingApproveController {

    @Reference(version = "1.0")
    MeetingApproveService meetingApproveService;

    /**
     * 审批记录
     *
     * @param meetingApproveOperateDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/approveOperate")
    public CommonResult approveOperate(MeetingApproveOperateDTO meetingApproveOperateDTO) {
        return meetingApproveService.approveOperate(meetingApproveOperateDTO);
    }

    /**
     * 查询审批列表
     *
     * @return
     */
    @ResponseBody
    @PostMapping("/getApproveList")
    public CommonResult<PageResult<MeetingApproveVO>> getApproveList(
        @RequestBody PageParam<MeetingApprovePageDTO> pageDTOPageParam) {
        CommonResult<PageResult<MeetingApproveVO>> approveList = meetingApproveService.getApproveList(pageDTOPageParam);

        return approveList;
    }

}
