package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 创建会议
 */
@Data
public class MeetingRoomContextDTO implements Serializable {

    /**
     * (创建不传，编辑传)会议id
     */
    private Long meetingRoomId;
    /**
     * （不需要）会议号
     */
    private String meetingCode;
    /**
     * （需要传）会议开始时间（GMT+8时间）。格 式：yyyy-MM-dd HH:mm。 说明 ● 创建预约会议时，如果没有指定 开始时间或填空串，则表示会议 马上开始
     */
    private Date startTime;
    /**
     * （需要传）会议持续时长，单位分钟。默认 30分钟。 最大1440分钟（24小时），最 小15分钟。
     */
    private Integer length;
    /**
     * （需要传）会议主题。最多128个字符。
     */
    private String subject;
    /**
     * （暂时不需要传）会议通知中会议时间的时区信 息。时区信息，参考时区映射关系。
     */
    private Integer timeZoneID = 56;
    /**
     * （需要传）资源类型
     */
    private String resourceType;
    /**
     * （需要传）资源id
     */
    private Integer resourceId;

    /**
     * （不需要传）资源状态
     */
    private Integer resourceStatus;

    /**
     * （不需要传） 华为资源id
     */
    private String vmrId;
    /**
     * （不需要传）华为云资源类型
     */
    private Integer vmrMode;

    /**
     * （需要传）是否免费 true: 免密 false: 需要密码
     */
    private Boolean guestPwdFlag;
    /**
     * （不需要传）用户等级
     */
    private Integer levelCode;
    /**
     * （不需要传）accid
     */
    private String imUserId;

    /**
     * （不需要传）用户名称
     */
    private String imUserName;
    /**
     * 与会者
     */
    private List<MeetingAttendeeDTO> attendees;
    /**
     * 备注
     */
    private String remark;
    /**
     * 语言编码
     */
    private String languageId;

    /**
     * 当前资源使用人id
     */
    private String currentResourceUserId;

}
