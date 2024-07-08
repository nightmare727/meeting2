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
import common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@RestController
@Slf4j
@RequestMapping(value = "/room")
@Tag(name = "会议管理接口")
public class MeetingController {

    @Reference(version = "1.0", timeout = 20000)
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
        log.info("企业级华为云事件回调入参：{}", hwEventReq);
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
     * 成功加入会议后回调
     *
     * @param joinMeetingRoomDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/enterMeetingRoom")
    public CommonResult enterMeetingRoom(@RequestBody JoinMeetingRoomDTO joinMeetingRoomDTO) {
        return rpcMeetingRoomService.enterMeetingRoom(joinMeetingRoomDTO);
    }

    /**
     * 获取空闲资源列表
     *
     * @param
     * @return
     */
    @ResponseBody
    @PostMapping("/getFreeResourceList")
    CommonResult<List<MeetingResourceVO>> getFreeResourceList(@RequestHeader("levelCode") Integer levelCode,
        @RequestHeader("finalUserId") String finalUserId, @RequestBody FreeResourceListDTO freeResourceListDTO) {
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
    CommonResult<MeetingRoomDetailDTO> createMeetingRoom(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("levelCode") Integer levelCode, @RequestHeader("userName") String userName,
        @RequestHeader(value = "language_id", defaultValue = "zh-CN") String languageId,
        @RequestHeader(value = "joyoCode") String joyoCode,
        @RequestHeader(value = "memberType") Integer memberType,
        @RequestBody MeetingRoomContextDTO meetingRoomContextDTO) throws Exception {
        meetingRoomContextDTO.setImUserId(finalUserId);
        meetingRoomContextDTO.setLevelCode(levelCode);
        meetingRoomContextDTO.setImUserName(userName);
        meetingRoomContextDTO.setLanguageId(languageId);
        meetingRoomContextDTO.setJoyoCode(joyoCode);
        meetingRoomContextDTO.setMemberType(memberType);
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
    CommonResult cancelMeetingRoom(@RequestHeader("finalUserId") String finalUserId,
        @RequestBody CancelMeetingRoomDTO cancelMeetingRoomDTO) {
        cancelMeetingRoomDTO.setImUserId(finalUserId);
        return rpcMeetingRoomService.cancelMeetingRoom(cancelMeetingRoomDTO);
    }

    /**
     * (旧版-建议更新)首页查询即将召开和进行中的会议列表
     *
     * @param finalUserId
     * @return
     */
    @ResponseBody
    @GetMapping("/getFutureAndRunningMeetingRoomList")
    CommonResult<FutureAndRunningMeetingRoomListVO> getFutureAndRunningMeetingRoomList(
        @RequestHeader("finalUserId") String finalUserId) {
        FutureAndRunningMeetingRoomListGetDTO futureAndRunningMeetingRoomListGetDTO =
            new FutureAndRunningMeetingRoomListGetDTO();

        futureAndRunningMeetingRoomListGetDTO.setFinalUserId(finalUserId);
        futureAndRunningMeetingRoomListGetDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
        return rpcMeetingRoomService.getFutureAndRunningMeetingRoomList(futureAndRunningMeetingRoomListGetDTO);
    }

    /**
     * (旧版-建议更新)查询历史会议列表
     *
     * @param
     * @return
     */
    @ResponseBody
    @GetMapping("/getHistoryMeetingRoomList/{month}")
    CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList(@RequestHeader("finalUserId") String finalUserId,
        @PathVariable("month") Integer month) {
        HistoryMeetingRoomListGetDTO historyMeetingRoomListGetDTO = new HistoryMeetingRoomListGetDTO();
        historyMeetingRoomListGetDTO.setFinalUserId(finalUserId);
        historyMeetingRoomListGetDTO.setMonth(month);
        historyMeetingRoomListGetDTO.setTimeZoneOffset(DateUtils.ZONE_STR_DEFAULT);
        return rpcMeetingRoomService.getHistoryMeetingRoomList(historyMeetingRoomListGetDTO);
    }

    /**
     * 首页查询即将召开和进行中的会议列表
     *
     * @param finalUserId
     * @return
     */
    @ResponseBody
    @PostMapping("/v19_0/getFutureAndRunningMeetingRoomList")
    CommonResult<FutureAndRunningMeetingRoomListVO> getFutureAndRunningMeetingRoomList(
        @RequestHeader("finalUserId") String finalUserId,
        @RequestBody FutureAndRunningMeetingRoomListGetDTO futureAndRunningMeetingRoomListGetDTO) {
        futureAndRunningMeetingRoomListGetDTO.setFinalUserId(finalUserId);

        return rpcMeetingRoomService.getFutureAndRunningMeetingRoomList(futureAndRunningMeetingRoomListGetDTO);
    }

    /**
     * 查询历史会议列表
     *
     * @param historyMeetingRoomListGetDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/v19_0/getHistoryMeetingRoomList")
    CommonResult<List<MeetingRoomDetailDTO>> getHistoryMeetingRoomList(@RequestHeader("finalUserId") String finalUserId,
        @RequestBody HistoryMeetingRoomListGetDTO historyMeetingRoomListGetDTO) {
        historyMeetingRoomListGetDTO.setFinalUserId(finalUserId);

        return rpcMeetingRoomService.getHistoryMeetingRoomList(historyMeetingRoomListGetDTO);
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
    CommonResult<List<ResourceTypeVO>> getMeetingResourceTypeList(@RequestHeader("finalUserId") String finalUserId,
        @RequestHeader("levelCode") Integer levelCode, @RequestHeader("nation_id") String nationId) {
        return rpcMeetingRoomService.getMeetingResourceTypeList(finalUserId, levelCode, nationId);
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
