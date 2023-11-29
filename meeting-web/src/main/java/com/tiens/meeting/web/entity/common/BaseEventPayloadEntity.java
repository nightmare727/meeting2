package com.tiens.meeting.web.entity.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/30
 * @Version 1.0
 */
@Data
public class BaseEventPayloadEntity implements Serializable {

    @Expose
    @SerializedName("operate_time")
    private Date operateTime;

}
