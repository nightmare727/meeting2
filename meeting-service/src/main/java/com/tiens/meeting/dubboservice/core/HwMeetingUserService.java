package com.tiens.meeting.dubboservice.core;

import com.tiens.api.vo.VMUserVO;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 */

public interface HwMeetingUserService {
    /**
     * 创建用户
     *
     * @param vmUserVO
     */
    public void addHwUser(VMUserVO vmUserVO);

    /**
     * 修改用户
     *
     * @param vmUserVO
     */
    public void modHwUser(VMUserVO vmUserVO);

}
