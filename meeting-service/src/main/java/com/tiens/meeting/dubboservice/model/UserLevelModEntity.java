package com.tiens.meeting.dubboservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLevelModEntity implements Serializable {

    private String accId;

    private Integer type;

    private Integer level;
}
