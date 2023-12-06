package com.tiens.api.service;

import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResourceTypeVO;
import com.tiens.api.vo.VMUserVO;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/11
 * @Version 1.0
 * @Company: tiens
 */
public interface RpcMeetingUserService {

    /**
     * 通过卓越卡号查询用户
     *
     * @param joyoCode
     * @return
     */
    CommonResult<VMUserVO> queryVMUser(String joyoCode,String accid) throws ServiceException ;


    /**
     * 通过卓越卡号添加用户
     *
     * @param joyoCode
     * @param resourceType
     * @return
     */
    CommonResult addMeetingHostUser(String joyoCode, Integer resourceType) throws ServiceException;


    /**
     * 通过accid添加会议用户
     *
     * @param accid
     * @return
     */
    CommonResult addMeetingCommonUser(String accid) throws ServiceException;


    /**
     * 移除主持人
     *
     * @param hostUserId
     * @return
     */
    CommonResult removeMeetingHostUser(Long hostUserId) throws ServiceException ;

    /**
     * 查询主持人信息
     *
     * @param accId
     * @return
     */
    CommonResult<MeetingHostUserVO> queryMeetingHostUser(String accId) throws ServiceException;

    /**
     * 分页查询主持人列表
     * @param pageDTOPageParam
     * @return
     */
    PageResult<MeetingHostUserVO> queryPage(PageParam<MeetingHostPageDTO> pageDTOPageParam) throws ServiceException ;

    /**
     * 查询会议资源配置列表
     * @return
     */
    CommonResult<List<MeetingResourceTypeVO>> queryResourceTypes();
}
