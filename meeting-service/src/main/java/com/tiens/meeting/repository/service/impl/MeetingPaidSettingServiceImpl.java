package com.tiens.meeting.repository.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.meeting.repository.mapper.MeetingPaidSettingPOMapper;
import com.tiens.meeting.repository.po.MeetingPaidSettingPO;
import com.tiens.meeting.repository.service.MeetingPaidSettingService;
import org.springframework.stereotype.Service;

@Service
public class MeetingPaidSettingServiceImpl extends ServiceImpl<MeetingPaidSettingPOMapper, MeetingPaidSettingPO> implements MeetingPaidSettingService {
}
