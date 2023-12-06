package com.tiens.meeting.dubboservice.consumer;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import com.huaweicloud.sdk.meeting.v1.model.BatchDeleteUsersRequest;
import com.huaweicloud.sdk.meeting.v1.model.BatchDeleteUsersResponse;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.dubboservice.model.UserLevelModEntity;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
import com.tiens.meeting.repository.po.MeetingLevelResourceConfigPO;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import com.tiens.meeting.repository.service.MeetingLevelResourceConfigDaoService;
import common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    MeetingLevelResourceConfigDaoService meetingLevelResourceConfigDaoService;


    @Override
    public void onMessage(MessageExt messageExt) {
        UserLevelModEntity userLevelModEntity=new UserLevelModEntity();
        try {
            String s = new String(messageExt.getBody(), "utf-8");
            userLevelModEntity = JSON.parseObject(s, UserLevelModEntity.class);
            log.info("用户等级修改收到消息：参数：{}", userLevelModEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        //逻辑校验   是否在表中有记录
        Optional<MeetingHostUserPO> meetingHostUserPO = meetingHostUserDaoService.lambdaQuery().eq(MeetingHostUserPO::getAccId, userLevelModEntity.getAccId()).oneOpt();
        if(!meetingHostUserPO.isPresent()){
            log.info("用户等级修改主持人表无此消息,参数：{}",userLevelModEntity);
            return;
        }

        //尝试修改主持人表
        boolean update = meetingHostUserDaoService.lambdaUpdate().eq(MeetingHostUserPO::getAccId, userLevelModEntity.getAccId())
                .set(MeetingHostUserPO::getLevel, userLevelModEntity.getLevel()).update();
        log.info("修改主持人等级结果：{}", update);
        MeetingHostUserPO hostUserPO = meetingHostUserPO.get();
        //判断主持人配置的会议资源 9级特殊处理 ,1到8级必须大于自身等级的资源方数
        if (userLevelModEntity.getLevel()==9){
            if (!(hostUserPO.getResourceType()>=7)){
                //移除不符合规则的主持人
                meetingHostUserDaoService.removeById(hostUserPO.getId());
            }
        }else{
            //1-8级逻辑处理
            MeetingLevelResourceConfigPO configPO = meetingLevelResourceConfigDaoService.lambdaQuery().eq(MeetingLevelResourceConfigPO::getVmUserLevel, userLevelModEntity.getLevel()).oneOpt().get();
            if (hostUserPO.getResourceType()<=configPO.getResourseType()){
                //移除不符合规则的主持人
                meetingHostUserDaoService.removeById(hostUserPO.getId());
            }
        }
    }
}
