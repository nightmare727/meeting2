/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tiens.meeting.web.controller;

import com.tiens.api.dto.*;
import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.*;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@RestController
@Slf4j
@RequestMapping(value = "/room")
public class MeetingController {

    @Reference
    RpcMeetingRoomService rpcMeetingRoomService;

    /**
     * 企业级华为云事件回调
     *
     * @param hwEventReq
     * @return
     */
    @ResponseBody
    @PostMapping("/openapi/meetingevent")
    public String addMeetingHostUser(@RequestBody HwEventReq hwEventReq) {
        CommonResult<String> stringCommonResult = rpcMeetingRoomService.updateMeetingRoomStatus(hwEventReq);
        return stringCommonResult.getData();
    }

    /**
     * 加入会议前置校验
     *
     * @param enterMeetingRoomCheckDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/enterMeetingRoomCheck")
    public CommonResult enterMeetingRoomCheck(@RequestHeader("finalUserId") String finalUserId,
        @RequestBody EnterMeetingRoomCheckDTO enterMeetingRoomCheckDTO) {
        enterMeetingRoomCheckDTO.setImUserId(finalUserId);
        return rpcMeetingRoomService.enterMeetingRoomCheck(enterMeetingRoomCheckDTO);
    }

    /**
     * 获取空闲资源列表
     *
     * @param
     * @return
     */
    @ResponseBody
    @GetMapping("/getFreeResourceList")
    CommonResult<List<MeetingResourceVO>> getFreeResourceList(@RequestHeader("levelCode") Integer levelCode,
        @RequestHeader("finalUserId") String finalUserId, @RequestParam("startTime") Date startTime,
        @RequestParam("length") Integer length, @RequestParam("resourceType") String resourceType) {
        FreeResourceListDTO freeResourceListDTO = new FreeResourceListDTO();
        freeResourceListDTO.setStartTime(startTime);
        freeResourceListDTO.setLength(length);
        freeResourceListDTO.setResourceType(resourceType);

        freeResourceListDTO.setLevelCode(levelCode);
        freeResourceListDTO.setImUserId(finalUserId);
        return rpcMeetingRoomService.getFreeResourceList(freeResourceListDTO);
    }

    /**
     * 创建、预约会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/createMeetingRoom")
    CommonResult createMeetingRoom(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("levelCode") Integer levelCode, @RequestHeader("userName") String userName,
        @RequestBody MeetingRoomContextDTO meetingRoomContextDTO) {
        meetingRoomContextDTO.setImUserId(finalUserId);
        meetingRoomContextDTO.setLevelCode(levelCode);
        meetingRoomContextDTO.setImUserName(userName);
        return rpcMeetingRoomService.createMeetingRoom(meetingRoomContextDTO);
    }

    /**
     * 编辑会议
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/updateMeetingRoom")
    CommonResult updateMeetingRoom(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("levelCode") Integer levelCode, @RequestHeader("userName") String userName,
        @RequestBody MeetingRoomContextDTO meetingRoomContextDTO) {
        meetingRoomContextDTO.setImUserId(finalUserId);
        meetingRoomContextDTO.setLevelCode(levelCode);
        meetingRoomContextDTO.setImUserName(userName);
        return rpcMeetingRoomService.updateMeetingRoom(meetingRoomContextDTO);

    }

    /**
     * 查询会议详情
     *
     * @param meetingRoomId
     * @return
     * @oaram imUserId
     */
    @ResponseBody
    @GetMapping("/getMeetingRoom/{meetingRoomId}")
    CommonResult<MeetingRoomDetailDTO> getMeetingRoom(@RequestHeader("finalUserId") String finalUserId,
        @PathVariable("meetingRoomId") Long meetingRoomId) {
        return rpcMeetingRoomService.getMeetingRoom(meetingRoomId, finalUserId);
    }

    /**
     * 根据会议号查询会议详情
     *
     * @param meetingCode
     * @return
     * @oaram imUserId
     */
    @ResponseBody
    @GetMapping("/getMeetingRoomByCode/{meetingCode}")
    CommonResult<MeetingRoomDetailDTO> getMeetingRoomByCode(@RequestHeader("finalUserId") String finalUserId,
        @PathVariable("meetingCode") String meetingCode) {
        return rpcMeetingRoomService.getMeetingRoomByCode(meetingCode);
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
     * 首页查询即将召开和进行中的会议列表
     *
     * @param finalUserId
     * @return
     */
    @ResponseBody
    @GetMapping("/getFutureAndRunningMeetingRoomList")
    CommonResult<FutureAndRunningMeetingRoomListVO> getFutureAndRunningMeetingRoomList(
        @RequestHeader("finalUserId") String finalUserId) {
        return rpcMeetingRoomService.getFutureAndRunningMeetingRoomList(finalUserId);
    }

    /**
     * 首页查询历史30天的会议列表
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/getHistoryMeetingRoomList/{month}")
    CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList(@RequestHeader("finalUserId") String finalUserId,
        @PathVariable("month") Integer month) {
        return rpcMeetingRoomService.getHistoryMeetingRoomList(finalUserId, month);
    }

    /**
     * 查询资源可用的时间段
     *
     * @param availableResourcePeriodGetDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/getAvailableResourcePeriod")
    CommonResult<List<AvailableResourcePeriodVO>> getAvailableResourcePeriod(
        @RequestHeader("finalUserId") String finalUserId,
        @RequestBody AvailableResourcePeriodGetDTO availableResourcePeriodGetDTO) {
        availableResourcePeriodGetDTO.setImUserId(finalUserId);
        return rpcMeetingRoomService.getAvailableResourcePeriod(availableResourcePeriodGetDTO);
    }

    /**
     * 查询会议录制详情
     *
     * @param meetingRoomId
     * @return
     */
    @ResponseBody
    @GetMapping("/getMeetingRoomRecordList/{meetingRoomId}")
    CommonResult<List<RecordVO>> getMeetingRoomRecordList(@PathVariable("meetingRoomId") Long meetingRoomId) {
        return rpcMeetingRoomService.getMeetingRoomRecordList(meetingRoomId);
    }

    /**
     * 查询会议类型列表
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/getMeetingResourceTypeList")
    CommonResult getMeetingResourceTypeList(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("levelCode") Integer levelCode) {
        return rpcMeetingRoomService.getMeetingResourceTypeList(finalUserId, levelCode);
    }

    /**
     * 查询某资源类型下全部资源
     *
     * @param resourceCode
     * @return
     */
    @ResponseBody
    @GetMapping("/getAllMeetingResourceList/{resourceCode}")
    CommonResult getAllMeetingResourceList(@PathVariable("resourceCode") String resourceCode) {
        return rpcMeetingRoomService.getAllMeetingResourceList(resourceCode);
    }

}
