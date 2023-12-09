package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.meeting.repository.mapper.MeetingResourceMapper;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Service;

/**
* @author yuwenjie
* @description 针对表【meeting_resouce】的数据库操作Service实现
* @createDate 2023-12-05 11:48:44
*/
@Service
public class MeetingResourceDaoServiceImpl extends ServiceImpl<MeetingResourceMapper, MeetingResourcePO>
    implements MeetingResourceDaoService {

    @Reference
    private MeetingResourceMapper meetingResourceMapper;

    /**
     * 更改会议资源状态:置为公有空闲或公有预约
     *
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingStatusById(String vmrId) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ vmrId);
        return meetingResourceMapper.update(vmrId);
    }
    @Override
    public int updateMeetingStatusById1(String vmrId) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ vmrId);
        return meetingResourceMapper.update1(vmrId);
    }

    /**
     * 分配会议资源
     * 根据accid==owner_im_user_id
     * 在数据表meeting_resource表中更改status
     * @return
     */
    @Override
    public int assignMeetingResource(String accId ) {
        log.debug("开始执行【更新资源状态】的数据访问，参数：{}"+ accId);
        return meetingResourceMapper.updateStatusByAccId(accId);
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
        return meetingResourceMapper.select(joyoCode);
    }



    /**
     * 通过vmrid查询accid
     *
     * @param vmrId
     * @return
     */
    @Override
    public String selectAccIdByVmrId(String vmrId) {
        return meetingResourceMapper.selectAccIdByVmrId(vmrId);
    }


    /**
     * 通过JoyoCode查询accid
     *
     * @param joyoCode
     * @return
     */
    @Override
    public String seleceAccIdByJoyoCode(String joyoCode) {
        return meetingResourceMapper.seleceAccIdByJoyoCode(joyoCode);
    }


    /**
     * 2公有预约 即为公有资源 有人预约时,可进行预分配操作
     * 操作后,此资源在此刻后，不可再被预约。当所有预约会议都结束后，此资源置为私有。
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingResourceStatusPrivate(String vmrId) {
        return meetingResourceMapper.updateMeetingResourceStatusPrivate(vmrId);
    }



    /**
     * 4设为公有空闲:在预分配状态资源下,可操作设为公有空闲
     * 操作后,此资源变为公有空闲,可被预约操作。
     * @param vmrId
     * @return
     */
    @Override
    public int updateMeetingResourceStatusPublicFree(String vmrId) {
        return meetingResourceMapper.updateMeetingResourceStatusPublicFree(vmrId);
    }

}


