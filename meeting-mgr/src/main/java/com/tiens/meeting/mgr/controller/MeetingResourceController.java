package com.tiens.meeting.mgr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiens.api.dto.*;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import common.util.io.ExcelUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
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

    private static final ObjectMapper sObjectMapper = new ObjectMapper();

    @Value("${excel.max-number:3000}")
    private int maxNumber;

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
        if (query.getCondition().getExport()) {
            // 限制100页
            query.setPageSize(maxNumber * 100);
            CommonResult<PageResult<MeetingRoomInfoDTO>> commonResult = rpcMeetingResourceService.queryMeetingRoomPage(query);
            try {
                ExcelUtil.downloadExcel(sObjectMapper.readTree(sObjectMapper.writeValueAsString(commonResult.getData().getList())),
                        Arrays.asList("资源ID", "云会议号", "资源名称",
                                "资源类型", "会议状态", "会议室类型",
                                "资源大小（人）", "预约时间", "预约人", "预约人ID",
                                "所在区域", "计划开始时间", "时长（分）",
                                "实际开始时间", "实际结束时间", "与会人数"),
                        Arrays.asList("resourceId", "hwMeetingCode", "resourceName",
                                "resourceTypeDesc", "stateDesc", "meetingRoomTypeDesc",
                                "size", "createTime", "ownerUserName", "ownerImUserId",
                                "area", "showStartTime", "duration",
                                "relStartTime", "relEndTime", "persons"
                        ), "会议列表");
            } catch (IOException e) {
                log.error("export error:{}", e.getMessage());
            }
            return null;
        }
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
    /**
     * 变更房间类型 仅支持公有与付费转换
     *
     * @param changeMeetingRoomType
     * @return
     */
    @ResponseBody
    @PostMapping("/changeType")
    public CommonResult changeType(@RequestBody ChangeMeetingRoomTypeDTO changeMeetingRoomType) {
        return rpcMeetingResourceService.changeMeetingRoomType(changeMeetingRoomType);
    }
}
