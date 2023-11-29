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

    int update(String vmrId);

    MeetingHostUserVO select(String joyoCode);

    int updateResouceByJoyoCode(String joyoCode);

    MeetingHostUserVO selectByOwnerUserId(Integer ownerUserId);
}




