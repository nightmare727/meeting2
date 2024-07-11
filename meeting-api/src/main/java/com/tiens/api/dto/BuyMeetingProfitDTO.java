package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class BuyMeetingProfitDTO implements Serializable {

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 用户id
     */
    private String finalUserId;

    /**
     * 卓越卡号
     */
    private String joyoCode;
    /**
     * 国家编码
     */
    private String nationId;


    /**
     * (需要)会议开始时间。格 式：yyyy-MM-dd HH:mm。
     */
    private Date startTime;

    /**
     * （需要传）时区偏移量
     */
    private String timeZoneOffset;
    /**
     * (需要)会议持续时长，单位分钟。默认 30分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length;

    /**
     * 前置时间
     */
    private Integer leadTime = 30;
}
