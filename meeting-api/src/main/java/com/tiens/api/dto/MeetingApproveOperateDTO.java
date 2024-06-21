package com.tiens.api.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import common.enums.MeetingApproveStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yuwenjie
 * @TableName meeting_approve
 */
@Data
public class MeetingApproveOperateDTO implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 会议状态 1：申请中 2：审批通过 3：审批驳回
     *
     * @see MeetingApproveStateEnum
     */
    private Integer approveStatus;

    /**
     * 审批备注
     */
    private String approveRemark;

}