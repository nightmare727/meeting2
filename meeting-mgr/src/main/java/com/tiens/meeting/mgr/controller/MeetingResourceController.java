package com.tiens.meeting.mgr.controller;

import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
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



}
