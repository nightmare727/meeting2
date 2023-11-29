package com.tiens.meeting.web.entity.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/30
 * @Version 1.0
 */
@Data
public class CommonWebHookDTO<T> implements Serializable {
    /**
     * 事件名
     */
    @Expose
    private String event;
    /**
     * 事件的唯一序列值
     */
    @Expose
    @SerializedName("trace_id")
    private String traceId;

    private List<T> payload;

}
