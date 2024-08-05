package com.tiens.api.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

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
     * 每天每次免费剩余预约次数
     */
    private Integer freeDayAppointCount;

    /**
     * 每场预约时长（分钟）
     */
    private Integer everyLimitCount;

    /**
     * 规格
     */
    private String resourceType;

    /**
     * 时间
     */
    @TableField(value = "go_time")
    private Date goTime;
}
