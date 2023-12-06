package com.tiens.meeting.dubboservice.core.impl;

import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.dubboservice.core.HwMeetingUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/6
 * @Version 1.0
 * @Company: tiens
 */
@Service()
@Slf4j
public class HwMeetingUserServiceImpl implements HwMeetingUserService {

    /**
     * 创建用户
     *
     * @param vmUserVO
     */
    @Override
    public void addHwUser(VMUserVO vmUserVO) {

    }

    /**
     * 修改用户
     *
     * @param vmUserVO
     */
    @Override
    public void modHwUser(VMUserVO vmUserVO) {

    }
}
