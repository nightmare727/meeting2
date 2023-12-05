package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 创建会议
 */
@Data
public class MeetingRoomCreateDTO implements Serializable {
    /**
     * 会议开始时间（UTC时间）。格 式：yyyy-MM-dd HH:mm。 说明 ● 创建预约会议时，如果没有指定 开始时间或填空串，则表示会议 马上开始 ● 时间是UTC时间，即0时区的时 间
     */
    private String startTime;
    /**
     * 会议持续时长，单位分钟。默认 30分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length;
    /**
     * 会议主题。最多128个字符。
     */
    private String subject;
    /**
     * 会议的媒体类型。 ● Voice：语音会议 ● HDVideo：视频会议
     */
    private String mediaTypes = "HDVideo";
    /**
     * 会议通知短信或邮件的语言。默 认中文。 ● zh-CN：中文 ● en-US：英文
     */
    private String language = "zh-CN";
    /**
     * 会议通知中会议时间的时区信 息。时区信息，参考时区映射关系。
     */
    private Integer timeZoneID;
    /**
     * 绑定给当前创会帐号的VMR ID。通过查询云会议室及个人会 议ID接口获取。 说明 ● vmrID取上述查询接口中返回的 id，不是vmrId ● 创建个人会议ID的会议时，使用 vmrMode=0的VMR；创建云会
     * 议室的会议时，使用 vmrMode=1的VMR
     */
    private String vmrID;

    /**
     * 来宾密码（4-16位长度的纯 数字）。
     */
    private String guestPwd;



}
