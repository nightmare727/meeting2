package com.tiens.meeting.dubboservice.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tiens.api.vo.VMUserVO;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.dubboservice.bo.MqCacheCleanBO;
import com.tiens.meeting.dubboservice.core.HwMeetingUserService;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.service.MeetingHostUserDaoService;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.util.RedisKeyCleanUtil;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
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

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/4
 * @Version 1.0
 * @Company: tiens
 * @desc im 用户修改监听
 */
@Component
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer.im_update_userinfo_group}",
    topic = "${rocketmq.consumer.im_update_userinfo_topic}", messageModel = MessageModel.CLUSTERING)
@Slf4j
public class UserInfoModifyConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Autowired
    MeetingHostUserDaoService meetingHostUserDaoService;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;

    @Reference(version = "1.0")
    DubboCommonUserService dubboCommonUserService;

    @Autowired
    HwMeetingUserService hwMeetingUserService;

    @Value("${rocketmq.producer.clean_cache_topic}")
    String cleanCacheTopic;

    @Override
    public void onMessage(MessageExt messageExt) {

        String keys = messageExt.getKeys();
        String imUserId = null;
        try {
            imUserId = new String(messageExt.getBody(), "utf-8");
            log.info("用户修改收到消费消息：参数：{}", imUserId);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //移除VM用户缓存
        SpringUtil.getBean(RedisKeyCleanUtil.class).sendCleanCacheMsg(
            new MqCacheCleanBO(cleanCacheTopic, RType.OBJECT, CacheKeyUtil.getUserInfoKey(imUserId), null));

        HomepageBo homepageBo = new HomepageBo();
        homepageBo.setAccId(imUserId);
        Result<HomepageUserDTO> dtoResult = dubboCommonUserService.queryUserInfoAccId(null, homepageBo);
        HomepageUserDTO data = dtoResult.getData();
        if (ObjectUtil.isEmpty(data)) {
            log.error("用户修改-查无此用户！,userId：{}", imUserId);
            return;
        }
        String accid = data.getAccid();
        String nickName = data.getNickName();
        String mobile = data.getMobile();
        String email = data.getEmail();
        //尝试修改主持人表
        boolean update = meetingHostUserDaoService.lambdaUpdate().eq(MeetingHostUserPO::getAccId, accid)
            .set(MeetingHostUserPO::getPhone, mobile).set(MeetingHostUserPO::getName, nickName)
            .set(MeetingHostUserPO::getEmail, email).update();
        log.info("修改主持人结果：{}", update);

//        meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getOwnerImUserId, accid)
//            .set(MeetingResourcePO::getOwnerImUserName, nickName).update();

        Boolean aBoolean = hwMeetingUserService.modHwUser(BeanUtil.copyProperties(data, VMUserVO.class));
        log.info("修改华为云用户信息结果：{}", aBoolean);
    }
}
