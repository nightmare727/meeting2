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
public class EventInfo implements Serializable {
    /**
     * 事件名
     */
    private String event;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 内容
     */
    private Payload payload;
}
