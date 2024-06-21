package com.tiens.api.dto;

import common.enums.MeetingApproveStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/6/12
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class MeetingApprovePageDTO implements Serializable {
    /**
     * 卓越卡号
     */
    private String joyoCode;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phoneNum;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 会议状态 1：申请中 2：审批通过 3：审批驳回
     *
     * @see MeetingApproveStateEnum
     */
    private Integer approveStatus;

    /**
     * 资源类型 1：中国区 2：海外区
     */
    private Integer resourceArea;

}
