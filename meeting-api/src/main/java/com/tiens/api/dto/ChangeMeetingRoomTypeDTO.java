package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 修改资源会议室类型
 * </p>
 *
 * @author MiaoQun
 * @since 2024-08-05 13:07:42
 */
@Data
public class ChangeMeetingRoomTypeDTO implements Serializable {
    private static final long serialVersionUID = 4991369216178645781L;
    /**
     * 资源id
     */
    private List<Integer> resourceIds;

    /**
     * 目标会议室类型
     */
    private Integer targetRoomType;
}
