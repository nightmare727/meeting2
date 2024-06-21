package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 大型会议审批表
 *
 * @TableName meeting_approve
 */
@TableName(value = "meeting_approve")
@Data
public class MeetingApprovePO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户accid
     */
    @TableField(value = "acc_id")
    private String accId;

    /**
     * 卓越卡号
     */
    @TableField(value = "joyo_code")
    private String joyoCode;

    /**
     * 姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 手机号
     */
    @TableField(value = "phone_num")
    private String phoneNum;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 会议状态 1：申请中 2：审批通过 3：审批驳回
     */
    @TableField(value = "approve_status")
    private Integer approveStatus;

    /**
     * 审批备注
     */
    @TableField(value = "approve_remark")
    private String approveRemark;

    /**
     * 资源类型 1：中国区 2：海外区
     */
    @TableField(value = "resource_area")
    private Integer resourceArea;

    /**
     * 申请人数
     */
    @TableField(value = "apply_person_num")
    private Integer applyPersonNum;

    /**
     * 分公司编码
     */
    @TableField(value = "sub_company_code")
    private String subCompanyCode;

    /**
     * 用途
     */
    @TableField(value = "use_to")
    private String useTo;

    /**
     * 开始时间
     */
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 持续时长（分钟数）
     */
    @TableField(value = "duration")
    private Integer duration;

    /**
     * 会议主题
     */
    @TableField(value = "meeting_topic")
    private String meetingTopic;
    /**
     * 时区偏移量
     */
    @TableField(value = "time_zone_offset")
    private String timeZoneOffset;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}