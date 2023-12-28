package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.meeting.repository.mapper.MeetingResouceMapper;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import org.springframework.stereotype.Service;

/**
 * @author yuwenjie
 */
@Service("userService")
public class MeetingResourceDaoServiceImpl extends ServiceImpl<MeetingResouceMapper, MeetingResourcePO>
    implements MeetingResourceDaoService {

}
