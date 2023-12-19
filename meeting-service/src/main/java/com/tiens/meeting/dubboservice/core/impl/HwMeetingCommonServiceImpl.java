package com.tiens.meeting.dubboservice.core.impl;

import cn.hutool.core.bean.BeanUtil;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.vo.RecordVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/11
 * @Version 1.0
 * @Company: tiens
 */
@Service
@Slf4j
public class HwMeetingCommonServiceImpl implements HwMeetingCommonService {

    @Autowired
    MeetingConfig meetingConfig;

    @Autowired
    public MeetingClient meetingClient;

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;

    public MeetingClient getUserMeetingClient(String imUserId) {
        MeetingCredentials auth =
            new MeetingCredentials().withAuthType(AuthTypeEnum.APP_ID).withAppId(meetingConfig.getAppId())
                .withAppKey(meetingConfig.getAppKey()).withUserId(imUserId);
        MeetingClient client =
            MeetingClient.newBuilder().withCredential(auth).withEndpoints(meetingConfig.getEndpoints()).build();
        return client;
    }

    /**
     * 分配云会议室
     *
     * @param imUserId
     * @param vmrIds
     */
    public void associateVmr(String imUserId, List<String> vmrIds) {
        AssociateVmrRequest request = new AssociateVmrRequest();
        request.withAccount(imUserId);
        request.withBody(vmrIds);
        request.setAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        AssociateVmrResponse response = meetingClient.associateVmr(request);
        log.info("分配云会议室结果：{}", response);
        for (String vmrId : vmrIds) {
            //设置当前使用者
            meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getVmrId, vmrId)
                .set(MeetingResourcePO::getCurrentUseImUserId, imUserId).update();
        }
        log.info("分配云会议室结果：{}", response);
    }

    /**
     * 回收云会议室
     *
     * @param imUserId
     * @param vmrIds
     */
    public void disassociateVmr(String imUserId, List<String> vmrIds) {
        DisassociateVmrRequest request = new DisassociateVmrRequest();
        request.withAccount(imUserId);
        request.withBody(vmrIds);
        request.setAccountType(AuthTypeEnum.APP_ID.getIntegerValue());
        try {
            DisassociateVmrResponse response = meetingClient.disassociateVmr(request);
            log.info("回收云会议室结果：{}", response);
            for (String vmrId : vmrIds) {
                //取消当前使用者
                meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getVmrId, vmrId)
                    .set(MeetingResourcePO::getCurrentUseImUserId, null).update();
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询录制详情
     *
     * @param confUUID
     */
    public List<RecordVO> queryRecordFiles(String confUUID) {
        ShowRecordingFileDownloadUrlsRequest request = new ShowRecordingFileDownloadUrlsRequest();
        request.withConfUUID(confUUID);
        request.withLimit(500);
        ShowRecordingFileDownloadUrlsResponse response = meetingClient.showRecordingFileDownloadUrls(request);
        List<RecordDownloadInfoBO> recordUrls = response.getRecordUrls();
        RecordDownloadInfoBO recordDownloadInfoBO = recordUrls.get(0);
        List<RecordDownloadUrlDO> urls = recordDownloadInfoBO.getUrls();
        List<RecordVO> recordVOS = BeanUtil.copyToList(urls, RecordVO.class);
        return recordVOS;

    }
}
