package com.tiens.api.service;

import com.tiens.api.dto.PushOrderDTO;
import common.pojo.CommonResult;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
public interface MemberProfitService {
    /**
     * 查询首页头图展示
     *
     * @return
     */
    CommonResult getCmsShow();

    /**
     * 推送订单
     *
     * @param pushOrderDTO
     * @return
     */
    CommonResult pushOrder(PushOrderDTO pushOrderDTO);
}
