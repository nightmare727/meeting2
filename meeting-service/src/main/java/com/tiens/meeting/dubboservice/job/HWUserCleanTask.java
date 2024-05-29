package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ClassUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingAttendeeDaoService;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingResourceStateEnum;
import common.enums.MeetingRoomStateEnum;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class HWUserCleanTask {

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Autowired
    MeetingAttendeeDaoService meetingAttendeeDaoService;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;

    @Autowired
    RedissonClient redissonClient;

    @XxlJob("HWUserCleanTaskJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog(description = "定时任务：清理华为用户")
    public void jobHandler() throws Exception {

        String lockName = CacheKeyUtil.BASE_CACHE_PREFIX + ClassUtil.getClassName(HWUserCleanTask.class, true);
        RLock lock = redissonClient.getLock(lockName);

        try {

            boolean b = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!b) {
                log.info("【定时任务：清理华为用户】未获取到锁");
                return;
            }

            MeetingClient mgrMeetingClient = hwMeetingCommonService.getMgrMeetingClient();
            SearchUsersRequest request = new SearchUsersRequest();
            request.withAdminType(SearchUsersRequest.AdminTypeEnum.NUMBER_2);
            request.withLimit(10);
            SearchUsersResponse searchUsersResponse = mgrMeetingClient.searchUsers(request);

            Integer count = searchUsersResponse.getCount();
            Integer maxHwUserCount = meetingConfig.getMaxHwUserCount();

            String maxHwUserThresholdPe = meetingConfig.getMaxHwUserThresholdPe();
            String minHwUserThresholdPe = meetingConfig.getMinHwUserThresholdPe();
            //当前华为会议人数
            BigDecimal bigDecimal1 = new BigDecimal(count);
            //最大华为会议人数
            BigDecimal bigDecimal2 = new BigDecimal(maxHwUserCount);
            //最大删除阈值
            BigDecimal bigDecimal3 = new BigDecimal(maxHwUserThresholdPe);
            //最小删除阈值
            BigDecimal bigDecimal4 = new BigDecimal(minHwUserThresholdPe);

            HashSet<@Nullable String> excludeAccidSet = Sets.newHashSetWithExpectedSize(1000);

            //判断当前用户数是否已达到华为最大用户阈值
            if (bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_UP).compareTo(bigDecimal3) >= 0) {
                //已超出

                //查询当前开会的人
                Set<String> meetingUserSet = getMeetingUsers();
                //查询私有资源的人
                Set<String> privateResourceUsers = getPrivateResourceUsers();

                Set<String> onlineUsers = getOnlineUser();

                //1、不删除当前正在开会、已入会的人
                //2、不删除分配专属会议资源的人

                excludeAccidSet.addAll(meetingUserSet);
                excludeAccidSet.addAll(privateResourceUsers);
                excludeAccidSet.addAll(onlineUsers);

                //删除华为用户直到最小华为用户阈值

                //确定需要删除的用户总数
                BigDecimal deleteCount = bigDecimal2.multiply(bigDecimal3.subtract(bigDecimal4));

                //确认删除次数
                int deleteTime = deleteCount.divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING).intValue();

                int mo = deleteCount.intValue() % 100;

                //再次查询华为用户列表
                for (int i = 0; i < deleteTime; i++) {
                    // 最大100
                    request.withOffset(i);

                    if (deleteTime == 1) {
                        request.withLimit(deleteCount.intValue());
                    } else if (i == (deleteTime - 1)) {
                        //最后一次
                        request.withLimit(mo);
                    } else {
                        request.withLimit(100);
                    }
                    SearchUsersResponse searchUsersResponse1 = mgrMeetingClient.searchUsers(request);

                    List<SearchUserResultDTO> data = searchUsersResponse1.getData();
                    Set<String> collect =
                        data.stream().map(SearchUserResultDTO::getThirdAccount).collect(Collectors.toSet());

                    //移除不需要删除的用户
                    collect.removeAll(excludeAccidSet);
                    //删除华为用户
                    doDeleteHwUser(collect);

                }

                log.info("【定时删除华为用户】共删除用户数：{}", deleteCount);
            }
        } catch (Exception e) {
            log.error("【定时删除华为用户】 执行异常", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Set<String> getOnlineUser() {
        HashSet<String> result = Sets.newHashSet();

        // 获取RKeys对象
        RKeys keys = redissonClient.getKeys();

        // 定义要查询的前缀
        String prefix = CacheKeyUtil.getBaseLoginUserInfoKey();

        // 使用getKeysByPattern方法查询具有固定前缀的缓存

        Iterable<String> keysWithPrefix = keys.getKeysByPattern(prefix + "*");
        for (String withPrefix : keysWithPrefix) {
            result.add(withPrefix.replace(prefix, ""));
        }
        log.info("登录用户数量为：{}", result.size());
        return result;
    }

    /**
     * 删除华为用户
     *
     * @param accIds
     * @return
     */
    Integer doDeleteHwUser(Set<String> accIds) {
        BatchDeleteUsersRequest batchDeleteUsersRequest = new BatchDeleteUsersRequest();
        batchDeleteUsersRequest.withAccountType(1);
        List<String> listbodyBody = new ArrayList<>();
        listbodyBody.addAll(accIds);
        batchDeleteUsersRequest.withBody(listbodyBody);
        //删除用户
        MeetingClient mgrMeetingClient = hwMeetingCommonService.getMgrMeetingClient();

        BatchDeleteUsersResponse response = mgrMeetingClient.batchDeleteUsers(batchDeleteUsersRequest);
        log.info("【定时删除华为用户】批次删除结果：{}", JSON.toJSONString(response));

        //删除缓存
        RMap<String, String> hwUserFlagMap = redissonClient.getMap(CacheKeyUtil.getHwUserSyncKey());
        String[] strings = accIds.stream().toArray(String[]::new);
        long l = hwUserFlagMap.fastRemove(strings);

        log.info("【定时删除华为用户】删除华为用户缓存结果数：{}", l);

        return accIds.size();
    }

    /**
     * 查询私有资源的人
     *
     * @return
     */
    private Set<String> getPrivateResourceUsers() {

        //查询私有资源
        List<MeetingResourcePO> list = meetingResourceDaoService.lambdaQuery()
            .eq(MeetingResourcePO::getStatus, MeetingResourceStateEnum.PRIVATE.getState()).list();
        Set<String> collect = list.stream().map(MeetingResourcePO::getOwnerImUserId).collect(Collectors.toSet());
        log.info("【定时任务：清理华为用户】查询私有资源 是{}人 ", collect.size());

        return collect;
    }

    /**
     * 查询当前开会的人
     *
     * @return
     */
    private Set<String> getMeetingUsers() {

        HashSet<@Nullable String> result = Sets.newHashSet();
        //查询正在开的会议
        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery().in(MeetingRoomInfoPO::getState,
                Lists.newArrayList(MeetingRoomStateEnum.Created.getState(), MeetingRoomStateEnum.Schedule.getState()))
            .list();
        if (CollectionUtil.isEmpty(list)) {
            return result;
        }
        ArrayList<@Nullable Long> meetingRoomIds = Lists.newArrayList();

        for (MeetingRoomInfoPO meetingRoomInfoPO : list) {
            //添加主持人
            result.add(meetingRoomInfoPO.getOwnerImUserId());
            meetingRoomIds.add(meetingRoomInfoPO.getId());
        }

        Set<String> AttendeeUserIds =
            meetingAttendeeDaoService.lambdaQuery().select(MeetingAttendeePO::getAttendeeUserId)
                .in(MeetingAttendeePO::getMeetingRoomId, meetingRoomIds).list().stream()
                .map(MeetingAttendeePO::getAttendeeUserId).collect(Collectors.toSet());

        result.addAll(AttendeeUserIds);

        log.info("【定时任务：清理华为用户】查询当前开会的是{}人 ", result.size());
        return result;
    }

    public static void main(String[] args) {
//        BigDecimal bigDecimal1 = new BigDecimal(10000);
//        BigDecimal bigDecimal2 = new BigDecimal("0.85");
//        BigDecimal bigDecimal3 = new BigDecimal("0.65");
//
//        BigDecimal multiply = bigDecimal1.multiply(bigDecimal2.subtract(bigDecimal3));
//
//        System.out.println(multiply.divide(BigDecimal.valueOf(3), 0, RoundingMode.CEILING).toString());
//
//        HashSet<String> strings = Sets.newHashSet("1", "2", "3");
//        String[] arr = strings.stream().toArray(String[]::new);
//
//        Arrays.stream(arr).forEach(System.out::println);

    }
}
