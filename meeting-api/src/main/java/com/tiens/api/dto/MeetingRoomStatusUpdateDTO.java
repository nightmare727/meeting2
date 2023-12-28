package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/7
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class MeetingRoomStatusUpdateDTO implements Serializable {

    private String event;

    private String meetingID;



}
