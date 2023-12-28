package com.tiens.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 谷守丙
 * @Date: 2023/12/6
 * @Version 1.0
 */
@Data
@Schema(description = "会议配置资源实体")
public class MeetingResourceTypeVO implements Serializable {
    /**
     * 会议资源配置类型
     */
    @Schema(description = "会议资源配置类型")
    private Integer resourceType;

    /**
     * 会议资源配置类型
     */
    @Schema(description = "会议资源配置上限")
    private Integer resourceNum;

    /**
     * 会议资源配置类型名称
     */
    @Schema(description = "会议资源配置类型")
    private String resourceTypeName;



}
