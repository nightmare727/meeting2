package com.tiens.meeting.dubboservice.consumer;

import com.alibaba.fastjson.JSON;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.dubboservice.model.UserLevelModEntity;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/4
 * @Version 1.0
 * @Company: tiens
 * @desc im 用户等级修改监听
 */
@Component
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer.im_user_level_group}",
    topic = "${rocketmq.consumer.im_user_level_topic}", messageModel = MessageModel.CLUSTERING)
@Slf4j
public class UserLevelModifyConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Autowired
    MeetingHostUserDaoService meetingHostUserDaoService;


    @Override
    public void onMessage(MessageExt messageExt) {

        try {
            String s = new String(messageExt.getBody(), "utf-8");
            UserLevelModEntity userLevelModEntity = JSON.parseObject(s, UserLevelModEntity.class);
            log.info("用户等级修改收到消息：参数：{}", userLevelModEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
