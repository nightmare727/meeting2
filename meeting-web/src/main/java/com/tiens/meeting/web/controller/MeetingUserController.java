package com.tiens.meeting.web.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tiens.api.dto.MeetingResourceAwardDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.web.entity.req.BatchQueryUserRequest;
import com.tiens.meeting.web.entity.req.QueryUserRequest;
import com.tiens.meeting.web.entity.resp.BatchQueryUserResponse;
import com.tiens.meeting.web.entity.resp.QueryUserResponse;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RequestMapping(value = "/mtuser")
@Slf4j
@RestController
public class MeetingUserController {

    @Reference(version = "1.0", timeout = 20000)
    RpcMeetingRoomService rpcMeetingRoomService;
    @Reference
    RpcMeetingUserService rpcMeetingUserService;

    /**
     * (废弃) 查询主持人信息
     *
     * @param accid
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryMeetingHostUser/{accid}")
    public CommonResult<MeetingHostUserVO> queryMeetingHostUser(@PathVariable("accid") String accid) throws Exception {
        CommonResult<MeetingHostUserVO> meetingHostUserVOCommonResult =
            rpcMeetingUserService.queryMeetingHostUser(accid);
        return meetingHostUserVOCommonResult;
    }

    /**
     * 查询VM用户信息
     *
     * @param finalUserId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/queryVMUser")
    public CommonResult<VMUserVO> queryVMUser(@RequestHeader("finalUserId") String finalUserId) throws Exception {
        return rpcMeetingUserService.queryVMUser(null, finalUserId);
    }

    /**
     * 查询直播用户
     *
     * @param queryUserRequest
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/queryLiveVMUser")
    public CommonResult<QueryUserResponse> queryLiveVMUser(@RequestBody QueryUserRequest queryUserRequest)
        throws Exception {
        CommonResult<VMUserVO> vmUserVOCommonResult =
            rpcMeetingUserService.queryVMUser(queryUserRequest.getUniqueSign(), queryUserRequest.getAccid());
        VMUserVO data = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(data)) {
            return CommonResult.success(null);
        }
        QueryUserResponse queryUserResponse = new QueryUserResponse();
        queryUserResponse.setUserId(data.getAccid());
        queryUserResponse.setNickName(data.getNickName());
        queryUserResponse.setUserPhone(data.getMobile());
        queryUserResponse.setUserPhoto(data.getHeadImg());
        //        queryUserResponse.setInviteCode();
        return CommonResult.success(queryUserResponse);
    }

    /**
     * 查询直播用户
     *
     * @param batchQueryUserRequest
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/batchQueryLiveVMUser")
    public CommonResult<List<BatchQueryUserResponse>> batchQueryLiveVMUser(
        @RequestBody BatchQueryUserRequest batchQueryUserRequest) throws Exception {

        List<QueryUserRequest> queryUserRequestList = batchQueryUserRequest.getQueryUserRequestList();

        List<BatchQueryUserResponse> collect = queryUserRequestList.stream().map(t -> {
            VMUserVO data = rpcMeetingUserService.queryVMUser(t.getUniqueSign(), t.getAccid()).getData();
            BatchQueryUserResponse batchQueryUserResponse = new BatchQueryUserResponse();
            if (ObjectUtil.isNotNull(data)) {

                batchQueryUserResponse.setUniqueSign(data.getJoyoCode());
                batchQueryUserResponse.setAccid(data.getAccid());

                QueryUserResponse queryUserResponse = new QueryUserResponse();
                queryUserResponse.setUserId(data.getAccid());
                queryUserResponse.setNickName(data.getNickName());
                queryUserResponse.setUserPhone(data.getMobile());
                queryUserResponse.setUserPhoto(data.getHeadImg());
                queryUserResponse.setInviteCode(data.getJoyoCode());
                queryUserResponse.setCountry(data.getCountry());
                queryUserResponse.setFansNum(data.getFansNum());
                queryUserResponse.setLevelCode(data.getLevelCode());
                batchQueryUserResponse.setData(queryUserResponse);
            } else {
                batchQueryUserResponse.setAccid(t.getAccid());
                batchQueryUserResponse.setUniqueSign(t.getUniqueSign());
            }
            return batchQueryUserResponse;
        }).collect(Collectors.toList());

        return CommonResult.success(collect);
    }

    /**
     * 查询登录认证
     *
     * @param accid
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/getCredential/{accid}")
    public CommonResult<VMMeetingCredentialVO> getCredential(@PathVariable("accid") String accid) throws Exception {
        //同步添加普通用户-必定成功
       /* ThreadUtil.execute(() -> {
            rpcMeetingUserService.addMeetingCommonUser(accid);
        });*/
        //查询登录认证
        return rpcMeetingRoomService.getCredential(accid);
    }

    /**
     * 增加会议主持人特权
     *
     * @param meetingResourceAwardDTO
     * @return
     */
    @ResponseBody
    @PostMapping("/addMeetingHostUser")
    CommonResult addMeetingHostUser(@RequestBody MeetingResourceAwardDTO meetingResourceAwardDTO) {
        CommonResult commonResult = rpcMeetingUserService.addMeetingHostUser(meetingResourceAwardDTO.getJoyoCode(),
            meetingResourceAwardDTO.getResourceType());
        return commonResult;
    }
}
