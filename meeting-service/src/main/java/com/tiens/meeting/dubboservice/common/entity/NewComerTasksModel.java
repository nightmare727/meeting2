package com.tiens.meeting.dubboservice.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/3/12
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewComerTasksModel implements Serializable {
    /**
     * 国家编码
     */
    private String country;
    /**
     * 渠道来源：1-平台、2-国内vmo币积分商城、3-VShare vmo币商城
     */
    private Integer source;
    /**
     * 任务类型 70-连续七日登录、71-商城首次下单、 72-首次购买藏品 、 73-首次使用会议、74-首次使用数字人 、75-首次玩游戏、76-首次使用AI
     */
    private Integer coinSource;

}
