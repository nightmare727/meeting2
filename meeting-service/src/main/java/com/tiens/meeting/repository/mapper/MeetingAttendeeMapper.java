package com.tiens.meeting.repository.mapper;

import com.tiens.meeting.repository.po.MeetingAttendeePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author yuwenjie
 * @description 针对表【meeting_attendee(会议与会者表)】的数据库操作Mapper
 * @createDate 2024-01-10 10:39:54
 * @Entity com.tiens.meeting.repository.po.MeetingAttendeePO
 */
public interface MeetingAttendeeMapper extends CommonMapper<MeetingAttendeePO> {

    /**
     * 通过房间id获取参会人数
     *
     * @param roomIds List<String>
     */
    List<Map<String, Object>> queryPersonsByRoomIds(@Param("roomIds") List<String> roomIds);
}



