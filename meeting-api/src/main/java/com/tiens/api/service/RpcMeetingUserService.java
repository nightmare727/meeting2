package com.tiens.api.service;

import com.tiens.api.dto.CommonProfitConfigSaveDTO;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.UserRequestDTO;
import com.tiens.api.vo.*;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;

import java.lang.reflect.InvocationTargetException;
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
     * @param level
     */
    CommonResult<List<MeetingResourceTypeVO>> queryResourceTypes(Integer level);



    /**
     * 会议黑名单
     *
     * @param
     * @param bean
     * @return
     */
    CommonResult<PageResult<MeetingBlackUserVO>> getBlackUserAll(PageParam<MeetingBlackUserVO> bean);

    /**
     * 解除黑名单用户
     * @param userId
     * @return
     */
    CommonResult deleteBlackUser(String userId);

    /**
     * 批量解除黑名单用户
     * @param userIdList
     * @return
     */
    CommonResult deleteBlackUserAll(List<String> userIdList);

    /**
     * 添加黑名单用户
     *
     * @param
     * @return
     */
    CommonResult addBlackUser(String account, UserRequestDTO userRequestDto);

    /**
     * 会议模版弹窗
     * @return
     */
    CommonResult PopupWindowList(List<LaugeVO> la);

    /**
     * 免费预约限制
     * @param meetingMemeberProfitConfigVOList
     * @return
     */
    CommonResult freeReservationLimit(List<MeetingMemeberProfitConfigVO> meetingMemeberProfitConfigVOList);

    /**
     * 开关接口
     * @param commonProfitConfigSaveDTO
     * @return
     */
    CommonResult opoCommonProfitConfig(CommonProfitConfigSaveDTO commonProfitConfigSaveDTO);

    /**
     * 回显
     *
     * @return
     */
    CommonResult<List<LaugeVO>> upPopupWindowList();

    /**
     * 查询会员权益表
     *
     * @return
     */
    CommonResult<List<MeetingMemeberProfitConfigVO>> queryCommonmeberProfitConfig() throws InvocationTargetException, IllegalAccessException;

    CommonResult queryMeetingBlackById(String userId);
}
