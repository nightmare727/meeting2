package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.meeting.repository.mapper.MeetingResouceMapper;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import com.tiens.meeting.repository.service.MeetingResouceDaoService;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service("userService")
public class MeetingResouceDaoServiceImpl extends ServiceImpl<MeetingResouceMapper,MeetingResoucePO> implements MeetingResouceDaoService {

    @Reference(version = "1.1")
    private MeetingResouceMapper meetingResouceMapper;

    @Override
    public int updateMeetingStatusById(String vmrId) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ vmrId);
        return meetingResouceMapper.update(vmrId);
    }

    @Override
    public int assignMeetingResouce(MeetingResouceIdDTO meetingResouceIdDTO) {
        Integer ownerUserId = meetingResouceIdDTO.getOwnerUserId();
        meetingResouceMapper.selectByOwnerUserId(ownerUserId);
        return 1;
    }

    @Override
    public MeetingHostUserVO selectUserByJoyoCode(String joyoCode) {
        log.debug("开始执行【查询主持人】的数据访问，参数：{}"+ joyoCode);
        return meetingResouceMapper.select(joyoCode);
    }

}
