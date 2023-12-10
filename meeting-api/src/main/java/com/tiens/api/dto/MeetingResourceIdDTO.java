package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MeetingResourceIdDTO implements Serializable {
    //account
    private String accId;
    //经销商号卓越卡号
    private String joyoCode;

}