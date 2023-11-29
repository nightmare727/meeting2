package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName meeting_time_zone_config
 */
@TableName(value ="meeting_time_zone_config")
@Data
public class MeetingTimeZoneConfigPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 时区id
     */
    @TableField(value = "time_zone_id")
    private Integer timeZoneId;

    /**
     * 时区偏移量
     */
    @TableField(value = "time_zone_offset")
    private String timeZoneOffset;

    /**
     * 英文描述
     */
    @TableField(value = "english_desc")
    private String englishDesc;

    /**
     * 中文描述
     */
    @TableField(value = "chinese_desc")
    private String chineseDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}