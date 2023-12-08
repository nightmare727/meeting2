package com.tiens.meeting.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import common.exception.ServiceException;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public interface MeetingResouceDaoService extends IService<MeetingResoucePO> {

    /**
     * 更改会议资源状态:置为公有空闲或公有预约
     *
     * @param vmrId
     * @return
     */
    int updateMeetingStatusById(String vmrId);
    int updateMeetingStatusById1(String vmrId);


    /**
     * 分配会议资源
     * 根据accid==owner_im_user_id,进行更改数据表操作
     * @param accId
     * @return
     */
    int assignMeetingResouce(String accId);

    /**
     * 查询主持人是否存在
     * 根据经销商账号卓越卡号joyoCode进行查询
     * @param joyoCode
     * @return
     */
    MeetingHostUserVO selectUserByJoyoCode(String joyoCode);


    /**
     * 通过vmrid查询accid
     *
     * @param vmrId
     * @return
     */
    String selectAccIdByVmrId(String vmrId);


    /**
     * 通过JoyoCode查询accid
     *
     * @param joyoCode
     * @return
     */
    String seleceAccIdByJoyoCode(String joyoCode);


    /**
     * 2公有预约 即为公有资源 有人预约时,可进行预分配操作
     * 操作后,此资源在此刻后，不可再被预约。当所有预约会议都结束后，此资源置为私有。
     * @param vmrId
     * @return
     */
    int updateMeetingResourceStatusPrivate(String vmrId);


    /**
     * 4设为公有空闲:在预分配状态资源下,可操作设为公有空闲
     * 操作后,此资源变为公有空闲,可被预约操作。
     *
     * @param vmrId
     * @return
     */
    int updateMeetingResourceStatusPublicFree(String vmrId);
}
