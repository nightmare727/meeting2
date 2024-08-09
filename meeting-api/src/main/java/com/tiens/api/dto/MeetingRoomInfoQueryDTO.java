package com.tiens.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 会议列表查询的dto
 * </p>
 *
 * @author zhank
 * @date 2024-08-02
 */
@Data
public class MeetingRoomInfoQueryDTO implements Serializable {

    /** 是否导出 */
    private boolean export = false;

    /** 云会议号 */
    private String hwMeetingCode;

    /** 会议开始时间 */
    private String relStartTime;

    /** 会议结束时间 */
    private String relEndTime;

    /** 资源大小 */
    private Integer resourceSize;

    /** 会议室类型 */
    private Integer meetingRoomType;

    /** 会议状态 */
    private String meetingState;

    /** 资源id */
    private String resourceId;

    /** 会议开始时间 */
    private String showStartTime;

    /** 会议结束时间 */
    private String showEndTime;

    public boolean getExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
