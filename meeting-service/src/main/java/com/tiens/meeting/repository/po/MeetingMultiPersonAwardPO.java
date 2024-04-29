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
 * @TableName meeting_multi_person_award
 */
@TableName(value ="meeting_multi_person_award")
@Data
public class MeetingMultiPersonAwardPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField(value = "im_user_id")
    private String imUserId;

    /**
     * 奖励人数阈值
     */
    @TableField(value = "award_size")
    private Integer awardSize;

    /**
     * 奖励次数
     */
    @TableField(value = "award_count")
    private Integer awardCount;

    /**
     * 备注   来自xxx会议新增了一次
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 更新时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}