package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.meeting.repository.po.MeetingOperationLogPO;
import com.tiens.meeting.repository.service.MeetingOperationLogService;
import com.tiens.meeting.repository.mapper.MeetingOperationLogMapper;
import org.springframework.stereotype.Service;

/**
* @author yuwenjie
* @description 针对表【meeting_operation_log】的数据库操作Service实现
* @createDate 2023-11-11 15:22:58
*/
@Service
public class MeetingOperationLogServiceImpl extends ServiceImpl<MeetingOperationLogMapper, MeetingOperationLogPO>
    implements MeetingOperationLogService{

}




