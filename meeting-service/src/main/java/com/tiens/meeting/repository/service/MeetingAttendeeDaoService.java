package com.tiens.meeting.repository.service;

import com.tiens.meeting.repository.po.MeetingAttendeePO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author yuwenjie
 * @description 针对表【meeting_attendee(会议与会者表)】的数据库操作Service
 * @createDate 2024-01-10 10:39:54
 */
public interface MeetingAttendeeDaoService extends IService<MeetingAttendeePO> {

    /**
     * 通过房间号获取参会人数
     * @param roomIds List<String>
     * @return Map
     */
    List<Map<String, Object>> queryPersonsByRoomIds(List<String> roomIds);
}
