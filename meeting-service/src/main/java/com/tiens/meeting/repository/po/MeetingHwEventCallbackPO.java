package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName meeting_hw_event_callback
 */
@TableName(value = "meeting_hw_event_callback")
@Data
public class MeetingHwEventCallbackPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * appid
     */
    @TableField(value = "app_id")
    private String appId;

    /**
     * 时间
     */
    @TableField(value = "timestamp")
    private Date timestamp;

    /**
     * 事件名
     */
    @TableField(value = "event")
    private String event;
    /**
     * 会议号
     */
    @TableField(value = "meeting_code")
    private String meetingCode;

    /**
     * 会议uuid
     */
    @TableField(value = "meeting_id")
    private String meetingId;


    /**
     * 回调数据
     */
    @TableField(value = "payload")
    private String payload;

    /**
     *
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}