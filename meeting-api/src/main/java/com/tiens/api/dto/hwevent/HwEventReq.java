package com.tiens.api.dto.hwevent;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/7
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class HwEventReq implements Serializable {
    /**
     * appId
     */
    private String appID;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 随机串
     */
    private String nonce;
    /**
     * 签证
     */
    private String signature;
    /**
     * 回调事件
     */
    private EventInfo eventInfo;
}
