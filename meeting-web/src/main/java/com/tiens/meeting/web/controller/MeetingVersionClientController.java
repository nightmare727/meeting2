package com.tiens.meeting.web.controller;

import com.tiens.api.service.RpcMeetingVersionService;
import com.tiens.api.vo.MeetingClientVersionVO;
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
@RequestMapping(value = "/version")
@Slf4j
@Tag(name = "客户端版本接口")
@RestController
public class MeetingVersionClientController {

    @Reference
    RpcMeetingVersionService rpcMeetingVersionService;

    /**
     * 查询客户端版本列表
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

}
