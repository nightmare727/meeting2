package com.tiens.meeting.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiens.api.dto.MeetingRoomInfoDTO;
import com.tiens.api.dto.MeetingRoomInfoQueryDTO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.tiens.meeting.repository.mapper.MeetingRoomInfoMapper;
import com.tiens.meeting.repository.service.MeetingRoomInfoDaoService;
import common.pojo.PageParam;
import org.springframework.stereotype.Service;

/**
 * @author yuwenjie
 * @description 针对表【meeting_room_info】的数据库操作Service实现
 * @createDate 2023-12-05 11:48:44
 */
@Service
public class MeetingRoomInfoDaoServiceImpl extends ServiceImpl<MeetingRoomInfoMapper, MeetingRoomInfoPO> implements MeetingRoomInfoDaoService {


    @Override
    public IPage<MeetingRoomInfoDTO> queryPage(PageParam<MeetingRoomInfoQueryDTO> query) {
        return baseMapper.pageQuery(new Page<>(query.getPageNum(), query.getPageSize()), query.getCondition());
    }
}




