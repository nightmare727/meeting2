package com.tiens.api.dto.hwevent;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/7
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class Payload implements Serializable {
    private MeetingInfo meetingInfo;
}
