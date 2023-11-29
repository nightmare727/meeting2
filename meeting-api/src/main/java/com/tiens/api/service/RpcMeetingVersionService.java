package com.tiens.api.service;

import com.tiens.api.dto.MeetingClientVersionDTO;
import com.tiens.api.vo.MeetingClientVersionVO;
import common.pojo.CommonResult;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
public interface RpcMeetingVersionService {

    CommonResult<List<MeetingClientVersionVO>> queryList();


    CommonResult saveMeetingClientVersion(MeetingClientVersionDTO meetingClientVersionDTO);

}
