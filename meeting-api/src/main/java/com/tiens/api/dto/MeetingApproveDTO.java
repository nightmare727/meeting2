package com.tiens.api.dto;

import common.enums.MeetingApproveStateEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 大型会议审批表
 *
 * @TableName meeting_approve
 */
@Data
public class MeetingApproveDTO implements Serializable {

    /**
     * 主键
     */
    private Integer id;
    /**
     * 用户accid
     */
    private String accId;

    /**
     * 卓越卡号
     */
    private String joyoCode;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phoneNum;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 会议状态 1：申请中 2：审批通过 3：审批驳回
     *
     * @see MeetingApproveStateEnum
     */
    private Integer approveStatus;

    /**
     * 审批备注
     */
    private String approveRemark;

    /**
     * 资源类型 1：中国区 2：海外区
     */
    private Integer resourceArea;

    /**
     * 申请人数
     */
    private Integer applyPersonNum;

    /**
     * 分公司编码
     */
    private String subCompanyCode;

    /**
     * 用途
     */
    private String useTo;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 持续时长（分钟数）
     */
    private Integer duration;

    /**
     * 会议主题
     */
    private String meetingTopic;

    /**
     * 时区偏移量
     */
    private String timeZoneOffset;

}