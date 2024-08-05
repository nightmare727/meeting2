package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会议用户黑名单表
 * @TableName meeting_black_user
 */
@TableName(value ="meeting_black_user")
@Data
public class MeetingBlackUserPO implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 卓越卡号
     */
    @TableField(value = "joyo_code")
    private String joyoCode;

    /**
     * 最后引发的会议号
     */
    @TableField(value = "last_meeting_code")
    private String lastMeetingCode;

    /**
     * 开始UTC时间
     */
    @TableField(value = "start_time")
    private Date startTime;

    /**
     * 结束UTC时间
     */
    @TableField(value = "end_time")
    private Date endTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}