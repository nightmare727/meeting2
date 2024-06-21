package com.tiens.api.service;

import com.tiens.api.dto.MeetingApproveDTO;
import com.tiens.api.dto.MeetingApproveOperateDTO;
import com.tiens.api.dto.MeetingApprovePageDTO;
import com.tiens.api.vo.MeetingApproveVO;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;

/**
 * @Author: 蔚文杰
 * @Date: 2024/6/12
 * @Version 1.0
 * @Company: tiens
 */
public interface MeetingApproveService {

    /**
     * 保存审批
     *
     * @param meetingApproveDTO
     * @return
     */
    CommonResult saveApprove(MeetingApproveDTO meetingApproveDTO);

    /**
     * 查询审批列表
     *
     * @param pageDTOPageParam
     * @return
     */
    CommonResult<PageResult<MeetingApproveVO>> getApproveList(PageParam<MeetingApprovePageDTO> pageDTOPageParam);

    /**
     * 操作审批
     *
     * @param meetingApproveOperateDTO
     * @return
     */
    CommonResult approveOperate(MeetingApproveOperateDTO meetingApproveOperateDTO);
}
