package com.tiens.meeting.dubboservice.consumer;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.meeting.dubboservice.bo.MqCacheCleanBO;
import com.tiens.meeting.dubboservice.model.UserLevelModEntity;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
import com.tiens.meeting.repository.po.MeetingLevelResourceConfigPO;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import com.tiens.meeting.repository.service.MeetingLevelResourceConfigDaoService;
import com.tiens.meeting.util.RedisKeyCleanUtil;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/4
 * @Version 1.0
 * @Company: tiens
 * @desc im 会员权益修改
 */
@Component
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer.meeting_im_member_profit_group}",
        topic = "${rocketmq.consumer.im_member_profit_topic}", messageModel = MessageModel.CLUSTERING)
@Slf4j
public class UserMemberProfitModifyConsumer implements RocketMQListener<MessageExt> {


    @Override
    public void onMessage(MessageExt messageExt) {
        //        UserLevelModEntity userLevelModEntity = new UserLevelModEntity();
        try {
            String s = new String(messageExt.getBody(), "utf-8");
            //            userLevelModEntity = JSON.parseObject(s, UserLevelModEntity.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //监听权益修改


        //1、会员升级降级都将用户权益使用记录表中的今日的数据状态置为失效

    }
}
