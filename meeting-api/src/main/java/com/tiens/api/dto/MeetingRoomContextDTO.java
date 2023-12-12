package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 创建会议
 */
@Data
public class MeetingRoomContextDTO implements Serializable {

    /**
     * 会议id
     */
    private Long meetingRoomId;

    private String meetingCode;
    /**
     * 会议开始时间（UTC时间）。格 式：yyyy-MM-dd HH:mm。 说明 ● 创建预约会议时，如果没有指定 开始时间或填空串，则表示会议 马上开始 ● 时间是UTC时间，即0时区的时 间
     */
    private Date startTime;
    /**
     * 会议持续时长，单位分钟。默认 30分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length;
    /**
     * 会议主题。最多128个字符。
     */
    private String subject;
    /**
     * 会议通知中会议时间的时区信 息。时区信息，参考时区映射关系。
     */
    private Integer timeZoneID = 56;
    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 资源状态
     */
    private Integer resourceStatus;

    /**
     * 华为资源id
     */
    private String vmrId;

    private Integer vmrMode;

    /**
     * 来宾密码（4-16位长度的纯 数字）。
     */
    private String guestPwd;
    /**
     * 用户等级
     */
    private Integer levelCode;
    /**
     * accid
     */
    private String imUserId;

    /**
     * 用户名称
     */
    private String imUserName;

}
