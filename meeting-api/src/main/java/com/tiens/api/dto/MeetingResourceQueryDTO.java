package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 会议资源分配后台查询参数
 * </p>
 *
 * @author MiaoQun
 * @since 2024-08-05 08:59:25
 */
@Data
public class MeetingResourceQueryDTO implements Serializable {
    private static final long serialVersionUID = -7928329114619535333L;

    /**
     * 云会议室名称
     */
    private String vmrName;

    /**
     * 资源大小
     */
    private Integer size;
    /**
     * 资源状态 1:空闲 2:预约
     */
    private Integer resourceStatus;
    /**
     * 会议室类型 2公有 3私有 4付费
     */
    private Integer meetingRoomType;
}
