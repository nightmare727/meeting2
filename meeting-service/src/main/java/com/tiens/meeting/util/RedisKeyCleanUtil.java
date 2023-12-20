package com.tiens.meeting.util;

import cn.hutool.core.util.ObjectUtil;
import com.tiens.meeting.dubboservice.bo.MqCacheCleanBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RMap;
import org.redisson.api.RType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/6
 * @Version 1.0
 * @Company: tiens
 */
@Component
@Slf4j
public class RedisKeyCleanUtil {
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    public SendResult sendCleanCacheMsg(MqCacheCleanBO mqCacheCleanBO) {
        //1、第一次清除
        cleanCache(mqCacheCleanBO);
        Message<MqCacheCleanBO> message = MessageBuilder.withPayload(mqCacheCleanBO).build();
        //等级为2，延迟5秒发送，第二次清除
        return rocketMQTemplate.syncSend(mqCacheCleanBO.getTopic(), message, 2000, 2);

    }

    public boolean cleanCache(MqCacheCleanBO mqCacheCleanBO) {
        //清除缓存
        if (ObjectUtil.isNull(mqCacheCleanBO)) {
            return false;
        }
        RType keyType = mqCacheCleanBO.getKeyType();
        String key = mqCacheCleanBO.getKey();
        String field = mqCacheCleanBO.getField();
        switch (keyType) {
            case OBJECT:
                //String
                boolean delete = redissonClient.getBucket(key).delete();
                log.info("删除 String 类型缓存：key：{}，结果：{}", key, delete);
                break;
            case MAP:
                //Map
                RMap<String, Object> map = redissonClient.getMap(key);
                if (StringUtils.isBlank(field)) {
                    //二级为空，清除整个一级缓存
                    map.delete();
                    log.info("删除 Map 类型缓存：key：{}", key);
                } else {
                    //二级key非空，删除部分
                    Object remove = map.remove(field);
                    log.info("删除 Map 类型缓存：key：{}， filed:{}， value：{}", key, field, remove);
                }
                break;
            case LIST:
                // list
                redissonClient.getList(key).delete();

                break;
            case SET:
                redissonClient.getSet(key).delete();
                break;
            case ZSET:
                redissonClient.getSortedSet(key).delete();
                break;
            default:
                log.info("不存在该数据类型：{}", keyType);
        }

        return true;
    }

}
