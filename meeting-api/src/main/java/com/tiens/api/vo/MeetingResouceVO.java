package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MeetingResouceVO implements Serializable {

    private Integer id;

    private String vmrId;

    private Integer vmrMode;

    private Date expireDate;

    private Integer size;

    private Integer status;

    private String name;

    private String joyoCode;
}
