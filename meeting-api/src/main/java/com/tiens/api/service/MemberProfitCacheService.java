package com.tiens.api.service;

import com.tiens.api.vo.MeetingMemeberProfitConfigVO;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 */
public interface MemberProfitCacheService {
    /**
     * 刷新会员权益缓存
     */
    void refreshMemberProfitCache();

    /**
     * 查询首页cms配置
     *
     * @return
     */
    Boolean getCmsShowEnabled();

    /**
     * 查询会员权益是否生效
     *
     * @return
     */
    Boolean getMemberProfitEnabled();

    /**
     * 查询会员权益
     *
     * @param memberType
     * @return
     */
    MeetingMemeberProfitConfigVO getMemberProfitConfig(Integer memberType);

}
