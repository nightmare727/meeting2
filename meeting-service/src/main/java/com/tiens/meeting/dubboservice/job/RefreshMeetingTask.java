package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import common.enums.MeetingRoomStateEnum;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2024/5/20
 * @Version 1.0
 * @Company: tiens
 */
//@Component
@Slf4j
public class RefreshMeetingTask {

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingAttendeeDaoService meetingAttendeeDaoService;

    @Autowired
    RedissonClient redissonClient;

    //    @XxlJob("RefreshMeetingTask")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog(description = "定时任务：刷新会议缓存")
    public void jobHandler() throws Exception {

        String lockName = CacheKeyUtil.BASE_CACHE_PREFIX + ClassUtil.getClassName(RefreshMeetingTask.class, true);
        RLock lock = redissonClient.getLock(lockName);

        try {

            boolean b = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!b) {
                log.info("【定时任务：刷新会议缓存】未获取到锁");
                return;
            }
            DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
            //查询进行中的会议
            List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery().in(MeetingRoomInfoPO::getState,
                    Lists.newArrayList(MeetingRoomStateEnum.Created.getState(),
                        MeetingRoomStateEnum.Schedule.getState()))
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();

            if (ObjectUtil.isNotEmpty(list)) {
                RMap<Long, Object> map = redissonClient.getMap(CacheKeyUtil.getMeetingCacheKey());
                Map<Long, MeetingRoomInfoPO> collect =
                    list.stream().collect(Collectors.toMap(MeetingRoomInfoPO::getId, Function.identity()));
                map.putAll(collect);
                log.info("【定时任务：刷新会议缓存】执行刷新覆盖条数为：{}", list.size());
            }

        } catch (Exception e) {
            log.error("【定时刷新会议缓存】 执行异常", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
