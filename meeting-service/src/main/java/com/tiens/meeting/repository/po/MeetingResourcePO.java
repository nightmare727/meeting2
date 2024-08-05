package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author yuwenjie
 * @TableName meeting_resouce
 */
@TableName(value = "meeting_resource")
@Data
public class MeetingResourcePO implements Serializable {
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
     * 资源状态 1:空闲 2:预约
     */
    @TableField(value = "resource_status")
    private Integer resourceStatus;
    /**
     * 是否存在预分配 1否 2是
     */
    @TableField(value = "pre_allocation")
    private Integer preAllocation;

    /**
     * 会议室类型 会议室类型 0未分配 2公有预约3 付费预约 4私人专属
     */
    @TableField(value = "meeting_room_type")
    private Integer meetingRoomType;

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
     * 当前使用人的accid
     */
    @TableField(value = "current_use_im_user_id")
    private String currentUseImUserId;

    /**
     * 资源类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    @TableField(value = "resource_type")
    private Integer resourceType;

    @TableField(value = "owner_im_user_name")
    private String ownerImUserName;
    @TableField(value = "owner_im_user_joyo_code")
    private String ownerImUserJoyoCode;
    /**
     * 私人专属到期时间的时间戳，单位毫秒
     */
    @TableField(value = "owner_expire_date")
    private Date ownerExpireDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingResourcePO that = (MeetingResourcePO)o;
        return Objects.equals(vmrId, that.vmrId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vmrId);
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}