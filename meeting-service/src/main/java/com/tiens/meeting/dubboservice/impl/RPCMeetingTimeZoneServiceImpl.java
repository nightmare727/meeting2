package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tiens.api.service.RPCMeetingTimeZoneService;
import com.tiens.api.vo.MeetingTimeZoneConfigVO;
import com.tiens.meeting.repository.po.MeetingTimeZoneConfigPO;
import com.tiens.meeting.repository.service.MeetingTimeZoneConfigDaoService;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/22
 * @Version 1.0
 * @Company: tiens
 */

@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RPCMeetingTimeZoneServiceImpl implements RPCMeetingTimeZoneService {

    private final MeetingTimeZoneConfigDaoService meetingTimeZoneConfigDaoService;

    @Override
    public CommonResult<List<MeetingTimeZoneConfigVO>> getList() {
        List<MeetingTimeZoneConfigPO> list = meetingTimeZoneConfigDaoService.list();
        return CommonResult.success(BeanUtil.copyToList(list, MeetingTimeZoneConfigVO.class));
    }
}
