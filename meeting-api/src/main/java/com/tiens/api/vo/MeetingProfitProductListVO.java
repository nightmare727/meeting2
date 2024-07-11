package com.tiens.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会议权益商品列表
 * @TableName meeting_profit_product_list
 */
@TableName(value ="meeting_profit_product_list")
@Data
public class MeetingProfitProductListVO implements Serializable {


    /**
     * 权益编码
     */
    private String profitCode;

    /**
     *  1：10方 2：50方 3：100方  4：200方 5：500方 6：1000方 7：3000方
     */
    private Integer resourceType;

    /**
     * 持续时长（分钟数）
     */
    private Integer duration;

    /**
     * vm币
     */
    private Integer vmCoins;


}