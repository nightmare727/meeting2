package com.tiens.meeting.mgr.controller;

import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.dto.MeetingResoucePageDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResouceVO;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
import com.tiens.api.vo.VMUserVO;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
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
    @PostMapping("/queryMeetingResoucePage")
    public CommonResult queryMeetingResoucePage(
            @RequestBody PageParam<MeetingResoucePageDTO> pageDTOPageParam) throws Exception {
        rpcMeetingResourceService.SearchCorpVmrSolution1();
        rpcMeetingResourceService.SearchCorpVmrSolution2();
        PageResult<MeetingResouceVO> vmUserVOCommonResult = rpcMeetingResourceService.queryMeetingResoucePage(pageDTOPageParam);
        return CommonResult.success(vmUserVOCommonResult);
    }


    /**
     * 3取消分配:私有状态即此资源属于某用户,可进行取消分配操作
     * 操作后，此资源变为公有。
     * 若此资源下有原来的预约会议,则为公有预约(不取消其在私有状态下的会议)
     * 若此资源下没有预约会议,则为公有空闲
     *
     * @param vmrId
     * @return
     */
    @ResponseBody
    @PostMapping("/updateMeetingStatus")
    public CommonResult updateMeetingStatus(String vmrId) throws Exception {
        CommonResult commonResult = rpcMeetingResourceService.updateMeetingStatus(vmrId);
        return commonResult;
    }

    ;


    /**
     * 1分配:公有空闲状态即公有资源无人预约时,可进行分配操作
     * 分配后此资源变为私有状态
     *
     * @param joyoCode
     * @return
     */
    @ResponseBody
    @PostMapping("/assignMeetingResouce")
    public CommonResult assignMeetingResouce(String joyoCode) throws Exception {
        CommonResult commonResult = rpcMeetingResourceService.assignMeetingResouce(joyoCode);
        return commonResult;
    }

    ;

    /**
     * 查询用户ID
     *
     * @param joyoCode
     * @return
     */
    @ResponseBody
    @PostMapping("/selectUserByJoyoCode")
    public CommonResult<MeetingHostUserVO> selectUserByJoyoCode(String joyoCode) throws Exception {
        CommonResult<MeetingHostUserVO> meetingHostUserVOCommonResult = rpcMeetingResourceService.selectUserByJoyoCode(joyoCode);
        if (meetingHostUserVOCommonResult != null) {
            return meetingHostUserVOCommonResult;
        } else {
            return null;
        }
    }


    /**
     * 2预分配:公有预约即为公有资源,有人预约时可进行 预分配 操作
     * 操作后,此资源在此刻后，不可再被预约。当所有预约会议都结束后，此资源置为私有。
     *
     * @param vmrId
     * @return
     */
    @ResponseBody
    @PostMapping("/updateMeetingResourceStatusPrivate")
    public CommonResult updateMeetingResourceStatusPrivate(String vmrId) throws Exception {
        CommonResult commonResult = rpcMeetingResourceService.updateMeetingResourceStatusPrivate(vmrId);
        return commonResult;
    }


    /**
     * 4设为公有空闲:在预分配状态资源下,可操作设为公有空闲
     * 操作后,此资源变为公有空闲,可被预约操作。
     *
     * @param vmrId
     * @return
     */
    @ResponseBody
    @PostMapping("/updateMeetingResourceStatusPublicFree")
    public CommonResult updateMeetingResourceStatusPublicFree(String vmrId) throws Exception {
        rpcMeetingResourceService.updateMeetingResourceStatusPublicFree(vmrId);
    return null;
    }
}
