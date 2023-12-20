package com.tiens.meeting.dubboservice.consumer;

import com.tiens.meeting.dubboservice.bo.MqCacheCleanBO;
import com.tiens.meeting.util.RedisKeyCleanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/4
 * @Version 1.0
 * @Company: tiens
 */
@Component
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer.clean_cache_group}",
    topic = "${rocketmq.producer.clean_cache_topic}", messageModel = MessageModel.CLUSTERING)
@Slf4j
public class CleanCacheConsumer implements RocketMQListener<MqCacheCleanBO> {

    @Autowired
    RedisKeyCleanUtil redisKeyCleanUtil;

    @Override
    public void onMessage(MqCacheCleanBO mqCacheCleanBO) {
        redisKeyCleanUtil.cleanCache(mqCacheCleanBO);
    }
}
