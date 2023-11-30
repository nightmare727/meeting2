package com.tiens.meeting.dubboservice.consumer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.AuthTypeEnum;
import com.huaweicloud.sdk.meeting.v1.model.ModUserDTO;
import com.huaweicloud.sdk.meeting.v1.model.UpdateUserRequest;
import com.huaweicloud.sdk.meeting.v1.model.UpdateUserResponse;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
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

    @Reference(version = "1.0")
    DubboCommonUserService dubboCommonUserService;

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
        //尝试修改华为云用户信息
        MeetingClient meetingClient = SpringUtil.getBean(MeetingClient.class);
        UpdateUserRequest request = new UpdateUserRequest();
        ModUserDTO body = new ModUserDTO();
//        String mobile = vmUserVO.getMobile();
//        if (ObjectUtil.isNotEmpty(mobile)) {
//            body.withPhone(mobile);
//        }
        //1-买买 2-云购 3 Vshare 4 瑞狮 5意涵永
        body.withName(StrUtil.brief(nickName, 64));
        body.setEmail(email);
        request.withBody(body);
        request.withAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        request.withAccount(accid);

        try {
            UpdateUserResponse updateUserResponse = meetingClient.updateUser(request);
            log.info("修改华为云用户结果：{}", updateUserResponse);
        } catch (Exception e) {
            log.info("修改华为云用户异常", e);
        }

    }
}
