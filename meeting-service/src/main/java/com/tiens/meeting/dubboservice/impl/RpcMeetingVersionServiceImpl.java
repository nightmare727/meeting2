package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tiens.api.dto.MeetingClientVersionDTO;
import com.tiens.api.service.RpcMeetingVersionService;
import com.tiens.api.vo.MeetingClientVersionVO;
import com.tiens.meeting.repository.po.MeetingClientVersionPO;
import com.tiens.meeting.repository.service.MeetingClientVersionDaoService;
import common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RpcMeetingVersionServiceImpl implements RpcMeetingVersionService {

    private final MeetingClientVersionDaoService meetingClientVersionDaoService;

    @Override
    public CommonResult<List<MeetingClientVersionVO>> queryList() {
        return CommonResult.success(
            BeanUtil.copyToList(meetingClientVersionDaoService.list(), MeetingClientVersionVO.class));
    }

    @Override
    public CommonResult saveMeetingClientVersion(MeetingClientVersionDTO meetingClientVersionDTO) {

        MeetingClientVersionPO meetingClientVersionPO =
            BeanUtil.copyProperties(meetingClientVersionDTO, MeetingClientVersionPO.class);
        boolean b = meetingClientVersionDaoService.saveOrUpdate(meetingClientVersionPO);
        return CommonResult.success(b);
    }
}
