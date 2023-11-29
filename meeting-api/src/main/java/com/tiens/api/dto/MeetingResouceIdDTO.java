package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MeetingResouceIdDTO implements Serializable {
    private Integer id;

    private Integer ownerUserId;

}
