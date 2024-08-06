package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 会议列表返回dto
 * </p>
 *
 * @author zhank
 * @date 2024-08-02
 */
@Data
public class MeetingRoomInfoDTO implements Serializable {

    /** 主键 */
    private String id;

    /** 资源id */
    private String resourceId;

    /** 云会议号 */
    private String hwMeetingCode;

    /** 资源名称 */
    private String resourceName;

    /** 资源类型枚举值 */
    private String resourceType;

    /** 资源类型 */
    private String resourceTypeDesc;

    /** 会议状态 */
    private String state;

    /** 会议状态 */
    private String stateDesc;

    /** 会议室类型 */
    private Integer meetingRoomType;

    /** 会议室类型描述 */
    private String meetingRoomTypeDesc;

    /** 资源大小 */
    private Integer size;

    /** 预约时间 */
    private String createTime;

    /** 预约人 */
    private String ownerUserName;

    /** 预约人id */
    private String ownerImUserId;
    /**
     * 所有者 Joyo Code
     */
    private String ownerJoyoCode;

    /** 所在区域 */
    private String area;

    /** 计划开始时间 */
    private String showStartTime;

    /** 时长 */
    private Integer duration;

    /** 实际开始时间 */
    private String relStartTime;

    /** 实际结束时间 */
    private String relEndTime;

    /** 参会人数 */
    private Integer persons;
}
