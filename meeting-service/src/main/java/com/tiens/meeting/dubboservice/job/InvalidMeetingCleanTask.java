package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingRoomStateEnum;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2024/5/20
 * @Version 1.0
 * @Company: tiens
 */
@Component
@Slf4j
public class InvalidMeetingCleanTask {

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingAttendeeDaoService meetingAttendeeDaoService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    ThreadPoolExecutor executorService =
        new ThreadPoolExecutor(16, 32, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10000),
            new ThreadFactoryBuilder().setNameFormat("hw-meeting-clean-%d").build());

    @XxlJob("HWUserCleanTaskJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog(description = "定时任务：清理无效会议")
    public void jobHandler() throws Exception {

        String lockName = CacheKeyUtil.BASE_CACHE_PREFIX + ClassUtil.getClassName(InvalidMeetingCleanTask.class, true);
        RLock lock = redissonClient.getLock(lockName);

        try {

            boolean b = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!b) {
                log.info("【定时任务：清理无效会议】未获取到锁");
                return;
            }
            DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));
            //查询进行中的会议
            List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery().in(MeetingRoomInfoPO::getState,
                    Lists.newArrayList(MeetingRoomStateEnum.Created.getState(),
                        MeetingRoomStateEnum.Schedule.getState()))
                .orderByAsc(MeetingRoomInfoPO::getLockStartTime).list();

            list = list.stream().filter(t -> {
                long startTime = t.getLockStartTime().getTime();
                long endTime = t.getLockEndTime().getTime();
                long middleTime = (startTime + endTime) / 2;
                long nowTime = now.getTime();
                return middleTime > nowTime;
            }).collect(Collectors.toList());
            if (ObjectUtil.isNotEmpty(list)) {
                //查询会议中是否有主持人入会
                List<MeetingAttendeePO> meetingAttendeePOList = meetingAttendeeDaoService.lambdaQuery()
                    .in(MeetingAttendeePO::getMeetingRoomId,
                        list.stream().map(MeetingRoomInfoPO::getHwMeetingId).collect(Collectors.toList())).list();

                Map<Long, Set<String>> collect = meetingAttendeePOList.stream().collect(
                    Collectors.groupingBy(MeetingAttendeePO::getMeetingRoomId,
                        Collectors.collectingAndThen(Collectors.toSet(),
                            e -> e.stream().map(MeetingAttendeePO::getAttendeeUserId).collect(Collectors.toSet()))));

                List<MeetingRoomInfoPO> invalidMeetingList = list.stream().filter(
                        t -> !collect.getOrDefault(t.getHwMeetingId(), Sets.newHashSet()).contains(t.getOwnerImUserId()))
                    .collect(Collectors.toList());

                if (ObjectUtil.isNotEmpty(invalidMeetingList)) {
                    for (MeetingRoomInfoPO meetingRoomInfoPO : invalidMeetingList) {
                        log.info("【定时任务：清理无效会议】,删除会议信息：{}", meetingRoomInfoPO.getConferenceId());
                        hwMeetingCommonService.stopMeeting(meetingRoomInfoPO.getConferenceId(),
                            meetingRoomInfoPO.getHostPwd());
                    }
                }
            }

        } catch (Exception e) {
            log.error("【定时清理无效会议】 执行异常", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
