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
 * @TableName meeting_host_user
 */
@TableName(value ="meeting_host_user")
@Data
public class MeetingHostUserPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 云信userId
     */
    @TableField(value = "acc_id")
    private String accId;

    /**
     * 经销商编号
     */
    @TableField(value = "joyo_code")
    private String joyoCode;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 级别
     */
    @TableField(value = "level")
    private Integer level;

    /**
     * 会议权限类型
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