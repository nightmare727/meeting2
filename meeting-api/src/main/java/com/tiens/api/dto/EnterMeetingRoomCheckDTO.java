package com.tiens.api.dto;

import common.util.date.DateUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class EnterMeetingRoomCheckDTO implements Serializable {
    /**
     * accid
     */
    private String imUserId;
    /**
     * 会议号
     */
    private String meetRoomCode;

    /**
     * （需要传）时区偏移量
     */
    private String timeZoneOffset= DateUtils.ZONE_STR_DEFAULT;
}
