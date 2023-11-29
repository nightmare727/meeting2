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
 * @Company: tiens
 */
@Data
public class MeetingRoomEntity implements Serializable {

    @Expose
    @SerializedName("meeting_id")
    private String meetingId;
    @Expose
    @SerializedName("meeting_code")
    private String meetingCode;
    @Expose
    private String subject;

    /**
     * 会议类型(0-一次性会议，1-周期性会议，2-微信专属会议，4-rooms投屏会议，5-个人会议号会议)
     */
    @Expose
    @SerializedName("meeting_type")
    private Integer meetingType;

    /**
     * 秒级别的会议开始时间戳
     */
    @Expose
    @SerializedName("start_time")
    private Date startTime;
    /**
     * 秒级别的会议结束时间戳
     */
    @Expose
    @SerializedName("end_time")
    private Date endTime;

    /**
     * 会议创建类型 0:普通会议；1:快速会议
     */
    @Expose
    @SerializedName("meeting_create_mode")
    private Integer meetingCreateMode;

    /**
     * 会议创建类型 0:普通会议；1:快速会议
     */
    @Expose
    @SerializedName("meeting_create_from")
    private Integer meetingCreateFrom;

    /**
     * 创建人
     */
    @Expose
    @SerializedName("creator")
    private MeetingUserEntity creator;

}
