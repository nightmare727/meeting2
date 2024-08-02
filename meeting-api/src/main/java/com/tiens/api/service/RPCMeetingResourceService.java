package com.tiens.api.service;

import com.tiens.api.dto.*;
import com.tiens.api.vo.MeetingApproveVO;
import com.tiens.api.vo.MeetingResourceVO;
import com.tiens.api.vo.MeetingRoomDetailDTO;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/22
 * @Version 1.0
 * @Company: tiens
 */
public interface RPCMeetingResourceService {

    /**
     * 分页查询会议资源列表
     *
     * @return
     */
    CommonResult<List<MeetingResourceVO>> queryMeetingResourceList() throws ServiceException;

    /**
     * 预分配
     *
     * @param resourceAllocateDTO
     * @return
     */
    CommonResult allocate(ResourceAllocateDTO resourceAllocateDTO);

    /**
     * 取消预分配
     *
     * @param cancelResourceAllocateDTO
     * @return
     */
    CommonResult cancelAllocate(CancelResourceAllocateDTO cancelResourceAllocateDTO);

    /**
     * 根据资源号查询会议列表
     *
     * @param resourceId
     * @return
     */
    CommonResult<List<MeetingRoomDetailDTO>> queryMeetingRoomList(Integer resourceId);

    /**
     * 分页查询会议列表
     *
     * @param query PageParam
     */
    CommonResult<PageResult<MeetingRoomInfoDTO>> queryMeetingRoomPage(PageParam<MeetingRoomInfoQueryDTO> query);
}
