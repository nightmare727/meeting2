package com.tiens.meeting.dubboservice.core;

import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.CreateConfTokenResponse;
import com.huaweicloud.sdk.meeting.v1.model.StopMeetingResponse;
import com.tiens.api.vo.RecordVO;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/11
 * @Version 1.0
 * @Company: tiens
 */
public interface HwMeetingCommonService {

    public MeetingClient getUserMeetingClient(String imUserId);

    /**
     * 分配云会议室
     *
     * @param imUserId
     * @param vmrIds
     */
    public void associateVmr(String imUserId, List<String> vmrIds);

    /**
     * 回收云会议室
     *
     * @param imUserId
     * @param vmrIds
     */
    public void disassociateVmr(String imUserId, List<String> vmrIds);

    /**
     * 查询录制详情
     *
     * @param confUUID
     */
    public List<RecordVO> queryRecordFiles(String confUUID);



    public CreateConfTokenResponse getCreateConfToken(String meetingCode, String hostPwd);

    public StopMeetingResponse stopMeeting(String meetingCode, String hostPwd);
}
