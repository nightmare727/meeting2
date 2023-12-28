package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.CancelMeetingRoomDTO;
import com.tiens.api.dto.CancelResourceAllocateDTO;
import com.tiens.api.dto.ResourceAllocateDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
import common.pojo.CommonResult;
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

    @Reference
    RpcMeetingRoomService rpcMeetingRoomService;

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
    @GetMapping("/queryMeetingResoucePage")
    public CommonResult<List<MeetingResourceVO>> queryMeetingResoucePage() {
        CommonResult<List<MeetingResourceVO>> listCommonResult = rpcMeetingResourceService.queryMeetingResourceList();
        return listCommonResult;
    }

    /**
     * 取消会议
     *
     * @param cancelMeetingRoomDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/cancelMeetingRoom")
    CommonResult cancelMeetingRoom(@RequestBody CancelMeetingRoomDTO cancelMeetingRoomDTO) {
        return rpcMeetingRoomService.cancelMeetingRoom(cancelMeetingRoomDTO);
    }

    /**
     * 查询资源相关会议列表
     *
     * @param resourceId
     * @return
     */
    @ResponseBody
    @GetMapping("/queryMeetingRoomList/{resourceId}")
    public CommonResult<List<MeetingRoomDetailDTO>> queryMeetingRoomList(
        @PathVariable("resourceId") Integer resourceId) {
        return rpcMeetingResourceService.queryMeetingRoomList(resourceId);
    }

    /**
     * 分配资源
     *
     * @param resourceAllocateDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/allocate")
    public CommonResult allocate(@RequestBody ResourceAllocateDTO resourceAllocateDTO) {
        return rpcMeetingResourceService.allocate(resourceAllocateDTO);
    }

    /**
     * 取消分配
     *
     * @param cancelResourceAllocateDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/cancelAllocate")
    public CommonResult cancelAllocate(@RequestBody CancelResourceAllocateDTO cancelResourceAllocateDTO) {
        return rpcMeetingResourceService.cancelAllocate(cancelResourceAllocateDTO);
    }

}
