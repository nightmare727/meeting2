package com.tiens.meeting.dubboservice.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yuwenjie
 * @TableName meeting_resouce
 */
@Data
public class MeetingRoomModel implements Serializable {

    /**
     * 华为云云会议室的ID
     */
    @TableField(value = "vmr_id")
    private String vmrId;

    /**
     * 云会议室的固定会议ID
     */
    @TableField(value = "vmr_conference_id")
    private String vmrConferenceId;

}