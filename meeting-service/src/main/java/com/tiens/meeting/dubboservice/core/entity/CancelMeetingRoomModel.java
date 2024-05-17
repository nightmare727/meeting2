package com.tiens.meeting.dubboservice.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/7
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
public class CancelMeetingRoomModel implements Serializable {

    private String imUserId;
    private String conferenceID;
    private String vmrId;

    private Boolean publicFlag;

    /**
     * 当前资源使用人id
     */
    private String currentResourceUserId;

    private Integer resourceId;

}
