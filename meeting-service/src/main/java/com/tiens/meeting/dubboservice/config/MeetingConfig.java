package com.tiens.meeting.dubboservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
@RefreshScope
@ConfigurationProperties(prefix = "meeting")
public class MeetingConfig {

    private String appId;

    private String appKey;
    /**
     * 存活时间秒数
     */
    private Integer expireSeconds;

    private List<String> endpoints;

    /**
     * 会议图标
     */
    private String meetingIcon;

    /**
     * 会议邀请标题key
     */
    private String inviteContentKey;
    /**
     * 会议邀请前置标题key
     */
    private String inviteImPrefixContentKey;

    /**
     * 邀请推送子标题
     */
    private String meetingInvitePushSubTitleKey;
    /**
     * 会议开始标题key
     */
    private String meetingStartContentKey;
    /**
     * 会议开始前置标题key
     */
    private String meetingStartPrefixContentKey;

    /**
     * 会议开始推送子标题key
     */
    private String meetingStartPushSubTitleKey;

    /**
     * 会议标题
     */
    private String meetingTitleKey;
    /**
     * 会议时间
     */
    private String meetingTimeKey;
    /**
     * 会议号
     */
    private String meetingCodeKey;
    /**
     * 推送子标题
     */
    private String meetingPushSubTitle;
    /**
     * 点击链接入会，或添加至会议列表 key
     */
    private String inviteUrlKey;
    /**
     * 邀请您参加V-Meeting会议
     */
    private String inviteTopicKey;

    /**
     * 会议密码
     */
    private String meetingPwdKey;
    /**
     * 新人奖励任务同步
     */
    private String newComerTasksSynUrl;

}
