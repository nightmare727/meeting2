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
     * 更改会议资源状态:置为公有空闲或公有预约
     *
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingStatusById(String vmrId) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ vmrId);
        return meetingResouceMapper.update(vmrId);
    }
    @Override
    public int updateMeetingStatusById1(String vmrId) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ vmrId);
        return meetingResouceMapper.update1(vmrId);
    }

    /**
     * 分配会议资源
     * 根据accid==owner_im_user_id
     * 在数据表meeting_resource表中更改status
     * @return
     */
    @Override
    public int assignMeetingResouce(String accId ) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ accId);
        return meetingResouceMapper.updateStatusByAccId(accId);
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
     * 通过vmrid查询accid
     *
     * @param vmrId
     * @return
     */
    @Override
    public String selectAccIdByVmrId(String vmrId) {
        return meetingResouceMapper.selectAccIdByVmrId(vmrId);
    }


    /**
     * 通过JoyoCode查询accid
     *
     * @param joyoCode
     * @return
     */
    @Override
    public String seleceAccIdByJoyoCode(String joyoCode) {
        return meetingResouceMapper.seleceAccIdByJoyoCode(joyoCode);
    }


    /**
     * 2公有预约 即为公有资源 有人预约时,可进行预分配操作
     * 操作后,此资源在此刻后，不可再被预约。当所有预约会议都结束后，此资源置为私有。
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingResourceStatusPrivate(String vmrId) {
        return meetingResouceMapper.updateMeetingResourceStatusPrivate(vmrId);
    }



    /**
     * 4设为公有空闲:在预分配状态资源下,可操作设为公有空闲
     * 操作后,此资源变为公有空闲,可被预约操作。
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingResourceStatusPublicFree(String vmrId) {
        return meetingResouceMapper.updateMeetingResourceStatusPublicFree(vmrId);
    }

}
