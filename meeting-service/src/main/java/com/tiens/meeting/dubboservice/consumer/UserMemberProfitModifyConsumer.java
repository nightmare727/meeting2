package com.tiens.meeting.dubboservice.consumer;

import com.alibaba.fastjson.JSON;
import com.tiens.api.dto.UserMemberProfitModifyEntity;
import com.tiens.api.service.MemberProfitService;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

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

    @Autowired
    MemberProfitService memberProfitService;

    @Override
    public void onMessage(MessageExt messageExt) {
        UserMemberProfitModifyEntity userMemberProfitModifyEntity = new UserMemberProfitModifyEntity();
        try {
            String s = new String(messageExt.getBody(), "utf-8");
            userMemberProfitModifyEntity = JSON.parseObject(s, UserMemberProfitModifyEntity.class);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("监听到会员等级变更，入参：{}", JSON.toJSONString(userMemberProfitModifyEntity));
        //监听权益修改
        CommonResult result = memberProfitService.modUserMemberProfit(userMemberProfitModifyEntity);



    }
}
