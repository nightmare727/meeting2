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
     * 更改会议资源状态:取消分配,操作后，此资源变为公有。
     *
     * @param vmrId
     * @return
     */
    CommonResult<MeetingResouceVO> updateMeetingStatus(String vmrId) throws ServiceException;

    /**
     * 公有空闲状态 即 公有资源 无人预约时
     *  可进行 分配操作,分配后
     *  此资源变为私有状态
     * @param joyoCode
     * @return
     */
    CommonResult<MeetingResouceVO> assignMeetingResouce(String joyoCode) throws ServiceException;

    /**
     * 查询主持人
     *
     * @param joyoCode
     * @return
     */
    MeetingHostUserVO selectUserByJoyoCode(String joyoCode);
}
