package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tiens.api.dto.CancelMeetingRoomDTO;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.po.MeetingBlackRecordPO;
import com.tiens.meeting.repository.po.MeetingBlackUserPO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingBlackRecordDaoService;
import com.tiens.meeting.repository.service.MeetingBlackUserDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.CommonStateEnum;
import common.enums.MeetingRoomStateEnum;
import common.util.cache.CacheKeyUtil;
import common.util.date.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
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

    @Autowired
    RpcMeetingRoomService rpcMeetingRoomService;
    @Autowired
    MeetingBlackRecordDaoService meetingBlackRecordDaoService;

    @Autowired
    MeetingBlackUserDaoService meetingBlackUserDaoService;

    ThreadPoolExecutor executorService =
        new ThreadPoolExecutor(16, 32, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10000),
            new ThreadFactoryBuilder().setNameFormat("hw-meeting-clean-%d").build());

    @XxlJob("InvalidMeetingCleanTask")
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
                return middleTime < nowTime;
            }).collect(Collectors.toList());
            if (ObjectUtil.isNotEmpty(list)) {
                //查询会议中是否有主持人入会
                List<MeetingAttendeePO> meetingAttendeePOList = meetingAttendeeDaoService.lambdaQuery()
                    .in(MeetingAttendeePO::getMeetingRoomId,
                        list.stream().map(MeetingRoomInfoPO::getId).collect(Collectors.toList())).list();

                Map<Long, Set<String>> collect = meetingAttendeePOList.stream().collect(
                    Collectors.groupingBy(MeetingAttendeePO::getMeetingRoomId,
                        Collectors.collectingAndThen(Collectors.toSet(),
                            e -> e.stream().map(MeetingAttendeePO::getAttendeeUserId).collect(Collectors.toSet()))));

                List<MeetingRoomInfoPO> invalidMeetingList = list.stream()
                    .filter(t -> !collect.getOrDefault(t.getId(), Sets.newHashSet()).contains(t.getOwnerImUserId()))
                    .collect(Collectors.toList());

                if (ObjectUtil.isNotEmpty(invalidMeetingList)) {
                    for (MeetingRoomInfoPO meetingRoomInfoPO : invalidMeetingList) {
                        log.info("【定时任务：清理无效会议】,删除会议信息：{}", meetingRoomInfoPO.getConferenceId());
                        String state = meetingRoomInfoPO.getState();

                        if (MeetingRoomStateEnum.Created.getState().equals(state)) {
                            //进行中的会议-停止
                            log.info("【定时任务：清理无效会议】,进行中的会议-停止,会议信息：{}",
                                meetingRoomInfoPO.getConferenceId());
                            hwMeetingCommonService.stopMeeting(meetingRoomInfoPO.getConferenceId(),
                                meetingRoomInfoPO.getHostPwd());
                        } else if (MeetingRoomStateEnum.Schedule.getState().equals(state)) {
                            //预约中的会议-取消
                            CancelMeetingRoomDTO cancelMeetingRoomDTO = new CancelMeetingRoomDTO();
                            cancelMeetingRoomDTO.setMeetingRoomId(meetingRoomInfoPO.getId());
                            cancelMeetingRoomDTO.setImUserId(meetingRoomInfoPO.getOwnerImUserId());
                            cancelMeetingRoomDTO.setReturnProfitFlag(Boolean.FALSE);
                            log.info("【定时任务：清理无效会议】,预约中的会议-取消,会议信息：{}",
                                meetingRoomInfoPO.getConferenceId());

                            rpcMeetingRoomService.cancelMeetingRoom(cancelMeetingRoomDTO);

                        }

                        //黑名单业务处理
                        judgeBlackUser(meetingRoomInfoPO);

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

    private void judgeBlackUser(MeetingRoomInfoPO meetingRoomInfoPO) {

        MeetingConfig.BlackUserConfigInner blackUserConfig = meetingConfig.getBlackUserConfig();

        //查询当前黑名单记录
        List<MeetingBlackRecordPO> list = meetingBlackRecordDaoService.lambdaQuery()
            .eq(MeetingBlackRecordPO::getUserId, meetingRoomInfoPO.getOwnerImUserId())
            .eq(MeetingBlackRecordPO::getStatus, CommonStateEnum.VALID.getState()).list();

        int count = list.size();

        log.info("【黑名单业务处理】，用户：{},会议号：{},当前异常次数为：{}", meetingRoomInfoPO.getOwnerImUserId(),
            meetingRoomInfoPO.getConferenceId(), count);

        MeetingBlackRecordPO meetingBlackRecordPO = new MeetingBlackRecordPO();
        meetingBlackRecordPO.setMeetingCode(meetingRoomInfoPO.getHwMeetingCode());
        meetingBlackRecordPO.setMeetingId(meetingRoomInfoPO.getId());
        meetingBlackRecordPO.setUserId(meetingRoomInfoPO.getOwnerImUserId());

        if (count + 1 >= blackUserConfig.getMaxTime()) {
            //达到最大黑名单次数

            //本次处理成无效
            meetingBlackRecordPO.setStatus(CommonStateEnum.INVALID.getState());
            //旧数据处理成无效
            list.stream().forEach(t -> t.setStatus(CommonStateEnum.INVALID.getState()));

            list.add(meetingBlackRecordPO);
            boolean b = meetingBlackRecordDaoService.saveOrUpdateBatch(list);

            log.info("【黑名单业务处理】，用户：{},会议号：{},当前异常次数为：{}，达到最大次数限制：{},执行结果：{}",
                meetingRoomInfoPO.getOwnerImUserId(), meetingRoomInfoPO.getConferenceId(), count,
                blackUserConfig.getMaxTime(), b);

            // 当前UTC时间
            DateTime startTime = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);

            DateTime endTime = DateUtil.offsetDay(startTime, 3);

            //用户成为黑名单一员
            MeetingBlackUserPO meetingBlackUserPO = new MeetingBlackUserPO();
            meetingBlackUserPO.setUserId(meetingRoomInfoPO.getOwnerImUserId());
//            meetingBlackUserPO.setJoyoCode();
            meetingBlackUserPO.setLastMeetingCode(meetingRoomInfoPO.getConferenceId());
            meetingBlackUserPO.setStartTime(startTime);
            meetingBlackUserPO.setEndTime(endTime);
            meetingBlackUserPO.setNickName(meetingRoomInfoPO.getOwnerUserName());
            meetingBlackUserPO.setMobile(meetingRoomInfoPO.getOwnerUserName());


            meetingBlackUserDaoService.save(meetingBlackUserPO);
        } else {
            //未达到最大限制
            meetingBlackRecordPO.setStatus(CommonStateEnum.VALID.getState());
            meetingBlackRecordDaoService.save(meetingBlackRecordPO);
        }

    }

}
