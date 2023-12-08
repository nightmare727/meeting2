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

import com.tiens.api.dto.hwevent.HwEventReq;
import com.tiens.api.service.RpcMeetingRoomService;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@RestController
@Slf4j
@RequestMapping(value = "/room")
public class MeetingController {

    @Reference
    RpcMeetingRoomService rpcMeetingRoomService;

    @ResponseBody
    @PostMapping("/openapi/meetingevent")
    public String addMeetingHostUser(@RequestBody HwEventReq hwEventReq) throws Exception {
        //算法hmacSHA256（appID + timestamp +nonce+ eventInfo，appkey）
        CommonResult<String> stringCommonResult = rpcMeetingRoomService.updateMeetingRoomStatus(hwEventReq);
        return stringCommonResult.getData();
    }


}
