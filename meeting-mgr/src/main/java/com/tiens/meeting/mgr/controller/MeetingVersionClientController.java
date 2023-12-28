package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.MeetingClientVersionDTO;
import com.tiens.api.service.RpcMeetingVersionService;
import com.tiens.api.vo.MeetingClientVersionVO;
import common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RequestMapping(value = "/version")
@Slf4j
@Tag(name = "客户端版本接口")
@RestController
public class MeetingVersionClientController {

    @Reference
    RpcMeetingVersionService rpcMeetingVersionService;

    /**
     * 查询客户端版版本列表
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryList")
    public CommonResult<List<MeetingClientVersionVO>> queryList() throws Exception {
        CommonResult<List<MeetingClientVersionVO>> listCommonResult = rpcMeetingVersionService.queryList();
        return listCommonResult;
    }

    /**
     * 保存客户端版本
     *
     * @param meetingClientVersionDTO
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/save")
    public CommonResult saveMeetingClientVersion(@RequestBody @Valid MeetingClientVersionDTO meetingClientVersionDTO)
        throws Exception {
        return rpcMeetingVersionService.saveMeetingClientVersion(meetingClientVersionDTO);
    }

}
