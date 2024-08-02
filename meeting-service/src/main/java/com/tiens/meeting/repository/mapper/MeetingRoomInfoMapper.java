package com.tiens.meeting.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiens.api.dto.MeetingRoomInfoDTO;
import com.tiens.api.dto.MeetingRoomInfoQueryDTO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author yuwenjie
* @description 针对表【meeting_room_info】的数据库操作Mapper
* @createDate 2023-12-05 11:48:44
* @Entity com.tiens.meeting.repository.po.MeetingRoomInfoPO
*/
public interface MeetingRoomInfoMapper extends BaseMapper<MeetingRoomInfoPO> {

    /**
     * <p> 分页查询 </p>
     *
     * @param query MeetingRoomInfoQueryDTO
     * @return IPage
     **/
    IPage<MeetingRoomInfoDTO> pageQuery(Page<MeetingRoomInfoDTO> page, @Param("param") MeetingRoomInfoQueryDTO query);
}




