package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.meeting.repository.po.MeetingRoomPO;
import com.tiens.meeting.repository.service.MeetingRoomService;
import com.tiens.meeting.repository.mapper.MeetingRoomMapper;
import org.springframework.stereotype.Service;

/**
* @author yuwenjie
* @description 针对表【meeting_room】的数据库操作Service实现
* @createDate 2023-11-11 15:22:58
*/
@Service
public class MeetingRoomServiceImpl extends ServiceImpl<MeetingRoomMapper, MeetingRoomPO>
    implements MeetingRoomService{

}




