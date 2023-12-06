package com.tiens.meeting.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import common.exception.ServiceException;


import org.springframework.context.annotation.Primary;


public interface MeetingResouceDaoService extends IService<MeetingResoucePO> {

    /**
     * 更改会议资源状态:置为空闲
     *
     * @param vmrId
     * @return
     */
    int updateMeetingStatusById(String vmrId);

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
}
