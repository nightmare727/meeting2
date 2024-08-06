package com.tiens.api.service;

import com.tiens.api.vo.MeetingPaidSettingVO;
import com.tiens.china.circle.api.dto.DubboUserInfoDTO;
import common.pojo.CommonResult;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 *
 *     会议缓存Service
 */
public interface MeetingCacheService {
    /**
     * 根据会议id刷新缓存
     *
     * @param meetingRoomIdList
     * @return
     */
    public CommonResult refreshMeetingRoomCache(List<Long> meetingRoomIdList);

    /**
     * 根据会议id删除缓存
     *
     * @param meetingRoomIdList
     * @return
     */
    public CommonResult deleteMeetingRoomCache(List<Long> meetingRoomIdList);

    /**
     * 修改用户缓存
     *
     * @param accId
     * @param joyoCode
     * @return
     */
    public CommonResult<DubboUserInfoDTO> refreshMeetingUserCache(String accId, String joyoCode);

    /**
     * 获取某个资源类型下的配置
     *
     * @param resourceType
     */
    CommonResult<MeetingPaidSettingVO> getMeetingPaidSettingByResourceType(Integer resourceType);
}
