package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class FreeResourceListDTO implements Serializable {

    private String imUserId;
    private Integer levelCode;
    /**
     * 会议开始时间（UTC时间）。格 式：yyyy-MM-dd HH:mm。 说明 ● 创建预约会议时，如果没有指定 开始时间或填空串，则表示会议 马上开始 ● 时间是UTC时间，即0时区的时 间
     */
    private Date startTime;
    /**
     * 会议持续时长，单位分钟。默认 30分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length;
}
