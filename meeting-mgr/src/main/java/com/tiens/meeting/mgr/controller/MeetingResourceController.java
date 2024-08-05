package com.tiens.meeting.mgr.controller;

import cn.hutool.core.date.DateUtil;
import com.tiens.api.dto.*;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import common.util.date.DateUtils;
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
    public CommonResult<List<MeetingResourceVO>> queryMeetingResoucePage(MeetingResourceQueryDTO meetingResourceQueryDTO) {
        CommonResult<List<MeetingResourceVO>> listCommonResult = rpcMeetingResourceService.queryMeetingResourceList(meetingResourceQueryDTO);
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
        if (resourceAllocateDTO.getOwnerExpireDate() != null &&
                DateUtil.convertTimeZone(resourceAllocateDTO.getOwnerExpireDate(), DateUtils.TIME_ZONE_GMT).isBefore(DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT))) {
            //分配资源的过期时间早于了当前时间
            log.error("【分配资源】分配资源的过期时间早于了当前时间:{}", resourceAllocateDTO);
            return CommonResult.error(GlobalErrorCodeConstants.CAN_NOT_ALLOCATE_RESOURCE);
        }
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

    /**
     * 分页查询会议列表
     *
     * @param query MeetingRoomInfoQueryDTO
     * @return MeetingRoomInfoDTO
     */
    @ResponseBody
    @PostMapping("queryMeetingRoomPage")
    public CommonResult<PageResult<MeetingRoomInfoDTO>> queryMeetingRoomPage(@RequestBody PageParam<MeetingRoomInfoQueryDTO> query) {
        return rpcMeetingResourceService.queryMeetingRoomPage(query);
    }

    /**
     * 取消会议
     *
     * @param meetingRoomUpDto MeetingRoomUpDto
     */
    @ResponseBody
    @PostMapping("closeMeeting")
    public CommonResult closeMeeting(@RequestBody MeetingRoomUpDTO meetingRoomUpDto) {
        return rpcMeetingRoomService.closeMeeting(meetingRoomUpDto);
    }

    /**
     * 结束会议
     *
     * @param meetingRoomUpDto MeetingRoomUpDto
     */
    @ResponseBody
    @PostMapping("stopMeeting")
    public CommonResult stopMeeting(@RequestBody MeetingRoomUpDTO meetingRoomUpDto) {
        return rpcMeetingRoomService.stopMeeting(meetingRoomUpDto);
    }

}
