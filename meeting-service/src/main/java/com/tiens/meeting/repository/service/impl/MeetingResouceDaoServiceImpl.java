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

    /**
     * 更改会议资源状态:置为空闲
     *
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingStatusById(String vmrId) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ vmrId);
        return meetingResouceMapper.update(vmrId);
    }

    /**
     * 分配会议资源
     * 根据经销商账号卓越卡号ownerUserId进行查询主持人是否存在
     * 如果存在则根据resouceId,ownerUserId进行修改数据库
     * @return
     */
    @Override
    public int assignMeetingResouce(MeetingResouceIdDTO meetingResouceIdDTO) {
        Integer ownerUserId = meetingResouceIdDTO.getOwnerUserId();
        MeetingHostUserVO meetingHostUserVO = meetingResouceMapper.selectByOwnerUserId(ownerUserId);
        if (meetingHostUserVO != null){
            Integer resouceId = meetingResouceIdDTO.getId();
            return meetingResouceMapper.updateStatusAndOwnerUserId(resouceId,ownerUserId);
        }
        return 1;
    }

    /**
     * 查询主持人是否存在
     * 根据经销商账号卓越卡号joyoCode进行查询
     * @param joyoCode
     * @return
     */
    @Override
    public MeetingHostUserVO selectUserByJoyoCode(String joyoCode) {
        log.debug("开始执行【查询主持人】的数据访问，参数：{}"+ joyoCode);
        return meetingResouceMapper.select(joyoCode);
    }

    /**
     * 将华为会议资源:1云会议室封装的PO存入到数据库
     *
     * @param meetingResoucePO
     * @return
     */
    @Override
    public void insertMeetingResoucePO(MeetingResoucePO meetingResoucePO) {
        meetingResouceMapper.insertinto(meetingResoucePO);
    }

    /**
     * 通过vmrid查询accid
     *
     * @param vmrId
     * @return
     */
    @Override
    public String selectAccIdByVmrId(String vmrId) {
        return meetingResouceMapper.selectAccIdByVmrId(vmrId);
    }

}
