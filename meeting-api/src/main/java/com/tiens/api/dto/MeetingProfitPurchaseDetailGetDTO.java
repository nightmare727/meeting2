package com.tiens.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2024/8/6
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class MeetingProfitPurchaseDetailGetDTO implements Serializable {

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 用户id
     */
    private String finalUserId;
    /**
     * 用户会员等级
     */
    private Integer memberType;

    /**
     * (需要)会议开始时间。格 式：yyyy-MM-dd HH:mm。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;

    /**
     * （需要传）时区偏移量
     */
    private String timeZoneOffset;

    /**
     * (需要)会议持续时长，单位分钟。默认 60分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length = 60;
    /**
     * 前置时间
     */
    private Integer leadTime;
}
