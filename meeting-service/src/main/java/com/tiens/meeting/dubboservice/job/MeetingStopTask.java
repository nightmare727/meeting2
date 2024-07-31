package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tiens.imchatapi.api.message.MessageService;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.dubboservice.impl.RpcMeetingRoomServiceImpl;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingResourceHandleEnum;
import common.enums.MeetingNewResourceStateEnum;
import common.enums.MeetingRoomStateEnum;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 *
 *     会议定时结束回收资源
 */
@Component
@Slf4j
public class MeetingStopTask {
    @Reference
    MessageService messageService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;
    @Autowired
    RpcMeetingRoomServiceImpl rpcMeetingRoomService;

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Autowired
    RetryTemplate retryTemplate;

    @Autowired
    RedissonClient redissonClient;

    @XxlJob("MeetingStopJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog(description = "定时任务：会议定时结束")
    public void jobHandler() throws Exception {

        String lockName = CacheKeyUtil.BASE_CACHE_PREFIX + ClassUtil.getClassName(MeetingStopTask.class, true);
        RLock lock = redissonClient.getLock(lockName);

        try {

            boolean b = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!b) {
                log.info("【定时任务：会议定时结束】未获取到锁");
                return;
            }

            //1、找到结束的会议室
            DateTime now = DateUtil.convertTimeZone(DateUtil.date(), ZoneId.of("GMT"));

            List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
                .in(MeetingRoomInfoPO::getState, Lists.newArrayList(MeetingRoomStateEnum.Schedule.getState(),
                    MeetingRoomStateEnum.Created.getState()))
                .le(MeetingRoomInfoPO::getLockEndTime, now).list();

            //排除掉私人专属会议
            list = list.stream().filter(m -> NumberUtil.isNumber(m.getResourceType())).collect(Collectors.toList());

            if (CollectionUtil.isEmpty(list)) {
                log.info("【定时任务：会议定时结束】:当前无需要处理的数据");
                return;
            }
            log.info("【定时任务：会议定时结束】 结束的会议参数:{}", JSON.toJSONString(list));
            //会议已经结束，修改会议状态
            for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
                //手动结束会议
                meetingRoomInfoDaoService.lambdaUpdate().eq(MeetingRoomInfoPO::getId, meetingRoomInfoPO.getId())
                    .set(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).update();
                //释放资源状态
                rpcMeetingRoomService.publicResourceHoldHandle(meetingRoomInfoPO.getResourceId(),
                    MeetingResourceHandleEnum.HOLD_DOWN);
                //回收资源
                MeetingResourcePO meetingResourcePO =
                    meetingResourceDaoService.getById(meetingRoomInfoPO.getResourceId());
                if (meetingRoomInfoPO.getState().equals(MeetingRoomStateEnum.Created.getState())) {
                    try {
                        hwMeetingCommonService.stopMeeting(meetingRoomInfoPO.getHwMeetingCode(),
                            meetingRoomInfoPO.getHostPwd());
                    } catch (Exception e) {
                        log.error("【定时任务：会议定时结束】停止华为云会议失败，异常", e);
                        throw e;
                    }
                }

                if (meetingRoomInfoPO.getOwnerImUserId()
                    .equals(meetingResourcePO.getCurrentUseImUserId()) && !meetingResourcePO.getStatus()
                    .equals(MeetingNewResourceStateEnum.PRIVATE.getState())) {
                    log.info("会议自动结束回收资源，会议数据：{}", meetingRoomInfoPO);
                    hwMeetingCommonService.disassociateVmr(meetingRoomInfoPO.getOwnerImUserId(),
                        Collections.singletonList(meetingResourcePO.getVmrId()));
                }

            }
            log.info("【定时任务：会议定时结束】会议定时结束完成，共执行：{}条", list.size());
        } catch (Exception e) {
            log.error("【定时任务：会议定时结束】 执行异常", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
