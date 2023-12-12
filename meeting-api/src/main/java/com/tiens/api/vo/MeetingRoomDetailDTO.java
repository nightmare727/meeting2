package com.tiens.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 详情
 */
@Data
public class MeetingRoomDetailDTO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 华为云会议id
     */
    private String hwMeetingId;

    /**
     * 华为会议code
     */
    private String hwMeetingCode;

    /**
     * 会议开始时间
     */
    private Date showStartTime;

    /**
     * 会议结束时间
     */
    private Date showEndTime;

    /**
     * 锁定开始时间
     */
    private Date lockStartTime;

    /**
     * 锁定结束时间
     */
    private Date lockEndTime;

    /**
     * 会议通知中会议时间的时区信息
     */
    private Integer timeZoneId;

    /**
     * 时区偏移量
     */
    private String timeZoneOffset;

    /**
     * 会议持续时长，单位分钟。默认30分钟。最大1440分钟（24小时），最小15分钟。
     */
    private Integer duration;
    /**
     * 主持人昵称
     */
    private String ownerUserName;

    /**
     * 会议状态。 ● “Schedule”：预定状态 ● “Creating”：正在创建状态 ● “Created”：会议已 经被创建，并正在召开 ● “Destroyed”：会议 已经关闭
     */
    private String state;
    /**
     * 录制状态 0：未录制完成  1：录制完成
     */
    private Integer recordStatus;

    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 资源类型
     */
    private Integer resourceType;
    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 主持人accid
     */
    private String ownerImUserId;

    /**
     * 主持人密码
     */
    private String chairmanPwd;

    /**
     * 嘉宾密码（4-16位长度的纯 数字）。
     */
    private String guestPwd;
    /**
     * 观众入会密码（网络研讨会专有）
     */
    private String audiencePasswd;

    /**
     * 与会者密码（云会议专有）
     */
    private String generalPwd;

    /**
     * 主持人入会地址。
     */
    private String chairJoinUri;

    /**
     * 嘉宾入会地址
     */
    private String guestJoinUri;

    /**
     * 观众入会地址
     */
    private String audienceJoinUri;


}
