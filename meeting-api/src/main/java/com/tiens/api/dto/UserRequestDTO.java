package com.tiens.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO implements Serializable {
    private List<String> userIdList;

    private Date endTime;
}
