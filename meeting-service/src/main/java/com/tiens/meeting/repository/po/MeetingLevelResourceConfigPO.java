package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName meeting_level_resource_config
 */
@TableName(value ="meeting_level_resource_config")
@Data
public class MeetingLevelResourceConfigPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户等级1到9级
     */
    @TableField(value = "vm_user_level")
    private Integer vmUserLevel;

    /**
     * 资源类型 1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    @TableField(value = "resourse_type")
    private Integer resourseType;

    /**
     * 资源数
     */
    @TableField(value = "resourse_num")
    private Integer resourseNum;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}