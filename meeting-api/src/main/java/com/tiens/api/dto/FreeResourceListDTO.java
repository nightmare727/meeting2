package com.tiens.api.dto;

import common.util.date.DateUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class FreeResourceListDTO implements Serializable {
    /**
     * (不需要)accid
     */
    private String imUserId;
    /**
     * (不需要)等级
     */
    private Integer levelCode;
    /**
     * (需要)会议开始时间。格 式：yyyy-MM-dd HH:mm。
     */
    private Date startTime;

    /**
     * （需要传）时区偏移量
     */
    private String timeZoneOffset = DateUtils.ZONE_STR_DEFAULT;
    /**
     * (需要)会议持续时长，单位分钟。默认 30分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length;
    /**
     * (需要)资源类型
     */
    private String resourceType;
}
