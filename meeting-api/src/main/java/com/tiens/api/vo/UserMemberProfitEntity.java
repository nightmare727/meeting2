package com.tiens.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMemberProfitEntity implements Serializable {

    /**
     * 1：普通会员 10： V++ 20: 红宝石V+ 30:蓝宝石+
     */
    private Integer memberType;

    /**
     * 剩余次数
     */
    private Integer surPlusCount;

    /**
     * 每天每次免费预约次数
     */
    private Integer freeDayAppointCount;

    /**
     * 每场预约时长（分钟）
     */
    private Integer everyLimitCount;

    /**
     * 规格数组逗号拼接，1，2，3，4等
     */
    private String resourceType;

    /**
     * 提前入会时间，逗号拼接，30.60等
     */
    private String goTime;
    /**
     * 资源规模方数数组
     */
    private List<Integer> resourceSizeList;

    /**
     * 提前开始时间数组
     */
    private List<Integer> leadTimeList;
}
