package com.tiens.api.service;

import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.dto.MeetingResoucePageDTO;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResouceVO;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
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
     * 查询会议资源信息
     *
     * @param vmrId
     * @return
     */
    CommonResult<MeetingResouceVO> queryMeetingResouce(String vmrId) throws ServiceException;

    /**
     * 分页查询会议资源列表
     * @param pageDTOPageParam
     * @return
     */
    PageResult<MeetingResouceVO> queryMeetingResoucePage(PageParam<MeetingResoucePageDTO> pageDTOPageParam) throws ServiceException;

    /**
     * 调取华为会议资源:1云会议室
     * @param
     * @return
     */
    void SearchCorpVmrSolution1()throws ServiceException;
    /**
     * 调取华为会议资源:2网络研讨会
     * @param
     * @return
     */
    void SearchCorpVmrSolution2()throws ServiceException;

    /**
     * 更改会议资源状态:置为空闲
     *
     * @param vmrId
     * @return
     */
    CommonResult<MeetingResouceVO> updateMeetingStatus(String vmrId) throws ServiceException;

    /**
     * 分配会议资源
     *
     * @param meetingResouceIdDTO
     * @return
     */
    CommonResult<MeetingResouceVO> assignMeetingResouce(MeetingResouceIdDTO meetingResouceIdDTO) throws ServiceException;

    /**
     * 查询主持人
     *
     * @param joyoCode
     * @return
     */
    MeetingHostUserVO selectUserByJoyoCode(String joyoCode);
}
