package com.tiens.api.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yuwenjie
 * @TableName meeting_resouce
 */
@Data
public class MeetingResourceVO implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 华为云云会议室的ID
     */
    private String vmrId;

    /**
     * 云会议室的固定会议ID
     */
    private String vmrConferenceId;

    /**
     * VMR模式 1：云会议室 2：网络研讨会
     */
    private Integer vmrMode;

    /**
     * 云会议室名称
     */
    private String vmrName;

    /**
     * 云会议室套餐名称
     */
    private String vmrPkgName;

    /**
     * 资源大小
     */
    private Integer size;

    /**
     * 资源状态 1:公有空闲 2:公有预约 3:私有 4:预分配
     */
    private Integer status;

    /**
     * 到期时间的时间戳，单位毫秒
     */
    private Date expireDate;

    /**
     * 私有者accid
     */
    private String ownerImUserId;

    /**
     * 资源类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    private String resourceType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private String ownerImUserName;
    private String ownerImUserJoyoCode;

}