package com.tiens.meeting.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiens.api.dto.MeetingApprovePageDTO;
import com.tiens.api.dto.MeetingRoomInfoDTO;
import com.tiens.api.dto.MeetingRoomInfoQueryDTO;
import com.tiens.meeting.repository.po.MeetingRoomInfoPO;
import com.baomidou.mybatisplus.extension.service.IService;
import common.pojo.PageParam;
import org.apache.ibatis.annotations.Param;

/**
 * @author yuwenjie
 * @description 针对表【meeting_room_info】的数据库操作Service
 * @createDate 2023-12-05 11:48:44
 */
public interface MeetingRoomInfoDaoService extends IService<MeetingRoomInfoPO> {

    /**
     * <p> 分页查询 </p>
     *
     * @param query MeetingRoomInfoQueryDTO
     * @return IPage
     **/
    IPage<MeetingRoomInfoDTO> queryPage(PageParam<MeetingRoomInfoQueryDTO> query);
}
