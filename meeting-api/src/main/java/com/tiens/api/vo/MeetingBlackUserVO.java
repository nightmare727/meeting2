package com.tiens.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束UTC时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 最大次数
     */
    private Integer maxTime;
    /**
     * 锁定天数
     */
    private Integer lockDay;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 国家编码
     */
    private String countryCode;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 操作人
     */
    private String operator;

    private String test;

    public String getTest() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = formatter.format(date);
        this.test = strDate;
        return test;
    }
}