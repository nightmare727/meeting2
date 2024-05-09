package com.tiens.meeting.dubboservice.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/4/23
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class UserExpAddEntity implements Serializable {

    /**
     * 经验值
     */
    private Integer experience;
    /**
     * 操作类型：1-增加、2-减少
     */
    private Integer operateType;

    /**
     * 来源类型
     */
    private Integer coinSource;

    /**
     * 国家编码
     */
    private String country = "cn";
    /**
     * 渠道来源：1-平台、2-国内vmo币积分商城、3-VShare vmo币商城
     */
    private Integer source = 1;

    /**
     * 订单号
     */
    private String orderNo = "";
    /**
     * 经验来源次数等信息，如：订单累计50，则值为50
     */
    private String coinSourceCode;
}
