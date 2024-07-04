package com.tiens.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户权益（涵盖收费、付费）
 */
@Data
public class MeetingUserProfitVO implements Serializable {
    /**
     * 会员用户权益
     */
    private UserMemberProfitEntity userMemberProfit;
    /**
     * 付费用户权益
     */
    private List<UserPaidProfitEntity> userPaidProfits;

}