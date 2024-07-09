package com.tiens.meeting.dubboservice.consumer;

import com.alibaba.fastjson.JSON;
import com.tiens.api.service.MeetingCacheService;
import com.tiens.meeting.dubboservice.model.UserLevelModEntity;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
import com.tiens.meeting.repository.po.MeetingLevelResourceConfigPO;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import com.tiens.meeting.repository.service.MeetingLevelResourceConfigDaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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

    @Value("${rocketmq.producer.clean_cache_topic}")
    String cleanCacheTopic;

    @Autowired
    MeetingCacheService meetingCacheService;

    @Override
    public void onMessage(MessageExt messageExt) {
        UserLevelModEntity userLevelModEntity = new UserLevelModEntity();
        try {
            String s = new String(messageExt.getBody(), "utf-8");
            userLevelModEntity = JSON.parseObject(s, UserLevelModEntity.class);
            log.info("用户等级修改收到消息：参数：{}", userLevelModEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        meetingCacheService.refreshMeetingUserCache(userLevelModEntity.getAccId(), null);

        //逻辑校验   是否在表中有记录
        Optional<MeetingHostUserPO> meetingHostUserPO =
            meetingHostUserDaoService.lambdaQuery().eq(MeetingHostUserPO::getAccId, userLevelModEntity.getAccId())
                .oneOpt();
        if (!meetingHostUserPO.isPresent()) {
            log.info("用户等级修改主持人表无此消息,参数：{}", userLevelModEntity);
            return;
        }

        //尝试修改主持人表
        boolean update =
            meetingHostUserDaoService.lambdaUpdate().eq(MeetingHostUserPO::getAccId, userLevelModEntity.getAccId())
                .set(MeetingHostUserPO::getLevel, userLevelModEntity.getLevel()).update();
        log.info("修改主持人等级结果：{}", update);
        MeetingHostUserPO hostUserPO = meetingHostUserPO.get();
        //判断主持人配置的会议资源 9级特殊处理 ,1到8级必须大于自身等级的资源方数
        if (userLevelModEntity.getLevel() == 9) {
            if (!(hostUserPO.getResourceType() >= 7)) {
                //移除不符合规则的主持人
                meetingHostUserDaoService.removeById(hostUserPO.getId());
            }
        } else {
            //1-8级逻辑处理
            MeetingLevelResourceConfigPO configPO = meetingLevelResourceConfigDaoService.lambdaQuery()
                .eq(MeetingLevelResourceConfigPO::getVmUserLevel, userLevelModEntity.getLevel()).oneOpt().get();
            if (hostUserPO.getResourceType() <= configPO.getResourceType()) {
                //移除不符合规则的主持人
                meetingHostUserDaoService.removeById(hostUserPO.getId());
            }
        }
    }
}
