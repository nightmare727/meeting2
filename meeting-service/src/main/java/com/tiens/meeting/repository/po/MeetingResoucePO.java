package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName meeting_resouce
 */
@TableName(value ="meeting_resouce")
@Data
public class MeetingResoucePO implements Serializable {
    /**
     * 主键
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
    private Integer status;

    /**
     * 到期时间的时间戳，单位毫秒
     */
    @TableField(value = "expire_date")
    private Date expireDate;

    /**
     * 私有者accid
     */
    @TableField(value = "owner_im_user_id")
    private String ownerImUserId;

    /**
     * 资源类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    @TableField(value = "resouce_type")
    private Integer resouceType;

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