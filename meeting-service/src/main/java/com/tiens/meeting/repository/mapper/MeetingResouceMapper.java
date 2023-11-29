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
     * 更改会议资源状态:置为空闲
     *
     * @param vmrId
     * @return
     */
    int update(String vmrId);

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
    MeetingHostUserVO selectByOwnerUserId(Integer ownerUserId);

    int updateStatusAndOwnerUserId(Integer resouceId,Integer ownerUserId);

    /**
     * 将华为会议资源:1云会议室封装的PO存入到数据库
     *
     * @param meetingResoucePO
     * @return
     */
    void insertinto(MeetingResoucePO meetingResoucePO);
}


