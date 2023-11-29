package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @TableName meeting_operation_log
 */
@TableName(value ="meeting_operation_log")
@Data
@EqualsAndHashCode
public class MeetingOperationLogPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件名
     */
    @TableField(value = "event")
    private String event;

    /**
     * 事件id
     */
    @TableField(value = "trace_id")
    private String traceId;

    /**
     * 操作人id
     */
    @TableField(value = "operator_user_id")
    private String operatorUserId;

    /**
     * 会议id
     */
    @TableField(value = "meeting_id")
    private String meetingId;

    /**
     * 操作时间
     */
    @TableField(value = "operate_time")
    private Date operateTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}