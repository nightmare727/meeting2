package com.tiens.meeting.web.controller;

import com.tiens.api.dto.MeetingApproveDTO;
import com.tiens.api.dto.MeetingApprovePageDTO;
import com.tiens.api.service.MeetingApproveService;
import com.tiens.api.vo.MeetingApproveVO;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 保存审批记录
     *
     * @param finalUserId
     * @param joyoCode
     * @param userName
     * @param phone
     * @param email
     * @param meetingApproveDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/saveApprove")
    public CommonResult saveApprove(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("joyoCode") String joyoCode, @RequestHeader("userName") String userName,
        @RequestHeader(value = "phone", required = false) String phone,
        @RequestHeader(value = "email", required = false) String email,
        @RequestBody MeetingApproveDTO meetingApproveDTO) {
        meetingApproveDTO.setAccId(finalUserId);
        meetingApproveDTO.setJoyoCode(joyoCode);
        meetingApproveDTO.setPhoneNum(phone);
        meetingApproveDTO.setName(userName);
        meetingApproveDTO.setEmail(email);
        return meetingApproveService.saveApprove(meetingApproveDTO);
    }

    /**
     * 查询审批列表
     *
     * @param joyoCode
     * @return
     */
    @ResponseBody
    @PostMapping("/getApproveList")
    public CommonResult<List<MeetingApproveVO>> getApproveList(@RequestHeader("joyoCode") String joyoCode) {

        PageParam<MeetingApprovePageDTO> meetingApprovePageDTOPageParam = new PageParam<>();
        meetingApprovePageDTOPageParam.setPageSize(1);
        meetingApprovePageDTOPageParam.setPageNum(Integer.MAX_VALUE);

        MeetingApprovePageDTO condition = new MeetingApprovePageDTO();
        condition.setJoyoCode(joyoCode);

        meetingApprovePageDTOPageParam.setCondition(condition);

        CommonResult<PageResult<MeetingApproveVO>> approveList =
            meetingApproveService.getApproveList(meetingApprovePageDTOPageParam);

        List<MeetingApproveVO> list = approveList.getData().getList();

        return CommonResult.success(list);
    }

}
