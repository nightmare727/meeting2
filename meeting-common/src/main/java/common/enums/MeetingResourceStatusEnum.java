package common.enums;

import com.huaweicloud.sdk.meeting.v1.model.MeetingStatus;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


public enum MeetingResourceStatusEnum {
    MEETING_RESOURCE_STATUS_PUBLIC_FREE(1,"公有空闲"),
    MEETING_RESOURCE_STATUS_PUBLIC_RESERVED(2,"公有预约"),
    MEETING_RESOURCE_STATUS_PRIVATE(3,"私有"),
    MEETING_RESOURCE_STATUS_PUBLIC_PRE_ALLOCATED(4,"公有预分配");

    @Getter
    private Integer code;
    @Getter
    private String desc;

    MeetingResourceStatusEnum(Integer status, String desc){
        this.code = code;
        this.desc = desc;
    }
}

