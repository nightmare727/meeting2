package com.tiens.meeting.web.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tiens.meeting.web.entity.common.BaseEventPayloadEntity;
import com.tiens.meeting.web.entity.common.MeetingRoomEntity;
import com.tiens.meeting.web.entity.common.MeetingUserEntity;
import lombok.Data;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/30
 * @Version 1.0
 */
@Data
public class MeetingCreateResult extends BaseEventPayloadEntity {



    @SerializedName("operator")
    @Expose
    private MeetingUserEntity operator;
    @SerializedName("meeting_info")
    @Expose
    private MeetingRoomEntity meetingInfo;
}
