package com.tiens.meeting.repository.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiens.api.vo.MeetingHostUserVO;

import com.tiens.meeting.repository.po.MeetingResoucePO;
import org.springframework.context.annotation.Primary;

/**
* @author 78267
* @description 针对表【meeting_resouce】的数据库操作Mapper
* @createDate 2023-11-28 00:03:55
* @Entity com.tiens.repository.po.MeetingResoucePO
*/
@Primary
public interface MeetingResouceMapper extends BaseMapper<MeetingResoucePO> {

    /**
     * 更改会议资源状态:置为公有空闲或公有预约
     *
     * @param vmrId
     * @return
     */
    int update(String vmrId);
    int update1(String vmrId);
    /**
     * 查询主持人是否存在
     * 根据经销商账号卓越卡号joyoCode进行查询
     * @param joyoCode
     * @return
     */
    MeetingHostUserVO select(String joyoCode);


    /**
     * 分配会议资源
     * 根据经销商账号卓越卡号ownerUserId进行查询主持人是否存在
     * 如果存在则根据resouceId,ownerUserId进行修改数据库
     * @return
     */
   /* MeetingHostUserVO selectByOwnerUserId(Integer ownerUserId);

    int updateStatusAndOwnerUserId(Integer resouceId,Integer ownerUserId);*/

    /**
     * 通过vmrid查询accid
     *
     * @param vmrId
     * @return accId
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
     * 分配会议资源
     * 根据accid==owner_im_user_id
     * 在数据表meeting_resource表中更改status从公有空闲变为私有状态
     * @return
     */
    int updateStatusByAccId(String accId);


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
     * @param vmrId
     * @return
     */
    int updateMeetingResourceStatusPublicFree(String vmrId);
}


