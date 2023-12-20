package com.tiens.meeting.dubboservice.bo;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/19
 * @Version 1.0
 */
@Data
@ToString
@Builder
public class FileCheckBO {

    private Long fileId;

    private String url;
}
