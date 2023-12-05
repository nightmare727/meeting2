package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName meeting_resource
 */
@TableName(value ="meeting_resource")
@Data
@EqualsAndHashCode
public class MeetingResoucePO implements Serializable {
    /**
     * 11111
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 华为云云会议室的ID
     */
    @TableField(value = "vmr_id")
    private String vmrId;

    /**
     * 云会议室的固定会议ID
     */
    @TableField(value = "vmr_conference_id")
    private String vmrConferenceId;

    /**
     * VMR模式 1：云会议室 2：网络研讨会
     */
    @TableField(value = "vmr_mode")
    private Integer vmrMode;

    /**
     * 云会议室名称
     */
    @TableField(value = "vmr_name")
    private String vmrName;

    /**
     * 云会议室套餐名称
     */
    @TableField(value = "vmr_pkg_name")
    private String vmrPkgName;

    /**
     * 资源大小
     */
    @TableField(value = "size")
    private Integer size;

    /**
     * 资源状态 0：正常 1：停用  2：未分配
     */
    @TableField(value = "status")
    private String status;

    /**
     * 到期时间的时间戳，单位毫秒
     */
    @TableField(value = "expire_date")
    private Date expireDate;

    /**
     * 主持人id
     */
    @TableField(value = "owner_user_id")
    private Long ownerUserId;

    /**
     * 资源类型
     */
    @TableField(value = "resource_type")
    private Integer resourceType;

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
