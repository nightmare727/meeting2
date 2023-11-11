package com.tiens.meeting.dubboservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/4
 * @Version 1.0
 * @Company: tiens
 * @desc im 用户修改监听
 */
@Component
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer.im_update_userinfo_group}", topic = "${rocketmq.consumer.im_update_userinfo_topic}",
    messageModel = MessageModel.BROADCASTING)
@Slf4j
public class UserInfoModifyConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String imUserId) {

        log.info("收到消费消息：参数：{}", imUserId);
    }
}
