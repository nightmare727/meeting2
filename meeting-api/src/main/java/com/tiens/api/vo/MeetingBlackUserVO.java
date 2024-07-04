package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会议用户黑名单表
 *
 * @TableName meeting_black_user
 */
@Data
public class MeetingBlackUserVO implements Serializable {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 卓越卡号
     */
    private String joyoCode;

    /**
     * 最后引发的会议号
     */
    private String lastMeetingCode;

    /**
     * 开始UTC时间
     */
    private Date startTime;

    /**
     * 结束UTC时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;

}