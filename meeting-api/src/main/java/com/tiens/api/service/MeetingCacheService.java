package com.tiens.api.service;

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

}
