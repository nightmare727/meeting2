package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.dubboservice.core.HwMeetingRoomHandler;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import common.enums.MeetingRoomStateEnum;
import common.util.cache.CacheKeyUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class RpcMeetingRoomDaoServiceImplTest {

    @Autowired
    RpcMeetingRoomService rpcMeetingRoomService;

    @Autowired
    MeetingRoomInfoDaoService meetingRoomInfoDaoService;

    @Resource
    Map<String, HwMeetingRoomHandler> hwMeetingRoomHandlers;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void getCredential() {
      /*  CommonResult<VMMeetingCredentialVO> commonResult =
            rpcMeetingRoomService.getCredential("h5v4qv8wl6916xld599q2vwkyrnncb9lfkj7kmh1");
        System.out.println(commonResult);*/
        RAtomicLong atomicLong = redissonClient.getAtomicLong(CacheKeyUtil.getHwMeetingRoomMaxSyncKey("1231"));
        System.out.println(atomicLong.get());
    }

    @Test
    void testSql() {
        DateTime startTime = DateUtil.date();
        DateTime endTime = DateUtil.date(startTime).offset(DateField.MINUTE, 30);
        Consumer<LambdaQueryWrapper<MeetingRoomInfoPO>> consumer =
            wrapper -> wrapper.ge(MeetingRoomInfoPO::getLockStartTime, startTime)
                .le(MeetingRoomInfoPO::getLockStartTime, endTime)
                .or(wrapper1 -> wrapper1.ge(MeetingRoomInfoPO::getLockEndTime, startTime)
                    .le(MeetingRoomInfoPO::getLockEndTime, endTime))
                .or(wrapper2 -> wrapper2.le(MeetingRoomInfoPO::getLockStartTime, startTime)
                    .ge(MeetingRoomInfoPO::getLockEndTime, endTime));

        List<MeetingRoomInfoPO> list = meetingRoomInfoDaoService.lambdaQuery()
            .ne(MeetingRoomInfoPO::getState, MeetingRoomStateEnum.Destroyed.getState()).nested(consumer).list();
        System.out.println(list);
    }

    @Test
    void testAutoWiredMap() {
        System.out.println(hwMeetingRoomHandlers);
    }

    @Test
    public void testHyperLogLogUnion() {
        String pfKey2 = "test:hll:02";
        String pfKey3 = "test:hll:05";
        String pfKey4 = "test:hll:06";
        for (int i = 0; i < 10; i++) {
            redisTemplate.opsForHyperLogLog().add(pfKey2, i);
        }
      /*  for (int i = 0; i < 20; i++) {
            redisTemplate.opsForHyperLogLog().add(pfKey3, i);
        }
        for (int i = 0; i < 30; i++) {
            redisTemplate.opsForHyperLogLog().add(pfKey4, i);
        }*/
        // 合并三组数
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, pfKey2, pfKey3, pfKey4);

        // 统计合并后的基数
        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }
}