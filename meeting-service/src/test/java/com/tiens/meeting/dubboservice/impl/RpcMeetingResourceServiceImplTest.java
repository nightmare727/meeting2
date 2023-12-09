package com.tiens.meeting.dubboservice.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.QueryOrgVmrResultDTO;
import com.huaweicloud.sdk.meeting.v1.model.SearchCorpVmrRequest;
import com.huaweicloud.sdk.meeting.v1.model.SearchCorpVmrResponse;
import com.tiens.api.vo.MeetingResourceSelectVO;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.repository.mapper.MeetingResourceMapper;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import common.enums.MeetingResourceEnum;
import common.enums.MeetingResourceStateEnum;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class RpcMeetingResourceServiceImplTest {

    @Autowired
    MeetingResourceDaoService meetingResourceDaoService;
    @Autowired
    MeetingResourceMapper meetingResourceMapper;

    @Test
    void getClient() {
        MeetingClient client = SpringUtil.getBean(MeetingClient.class);
        System.out.println("打印:" + client);
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();

        request.withVmrMode(2);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                System.out.println("打印打印打印打印打印打印打印item:" + item);
                String vmrId = item.getId();
                MeetingResourceSelectVO meetingResourceSelectVO = meetingResourceMapper.selectVOByVMR(vmrId);
                if (meetingResourceSelectVO == null) {

                    MeetingResourcePO meetingResourcePO = new MeetingResourcePO();
                    meetingResourcePO.setVmrId(item.getId());
                    meetingResourcePO.setVmrConferenceId(item.getVmrId());
                    meetingResourcePO.setVmrMode(2);
                    meetingResourcePO.setVmrName(item.getVmrName());
                    meetingResourcePO.setVmrPkgName(item.getVmrPkgName());
                    meetingResourcePO.setSize(item.getMaxAudienceParties());

                    Integer status = item.getStatus();
                    if (status == 1) {
                        meetingResourcePO.setStatus(MeetingResourceStateEnum.PUBLIC_FREE.getState());
                    } else if (status == 2) {
                        meetingResourcePO.setStatus(MeetingResourceStateEnum.PUBLIC_SUBSCRIBE.getState());
                    } else if (status == 3) {
                        meetingResourcePO.setStatus(MeetingResourceStateEnum.PRIVATE.getState());
                    } else if (status == 4) {
                        meetingResourcePO.setStatus(MeetingResourceStateEnum.REDISTRIBUTION.getState());
                    }

                    Integer vmrPkgParties = item.getMaxAudienceParties();
                    if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_10.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_10.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_50.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_50.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_100.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_100.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_200.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_200.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_500.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_500.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_1000.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_1000.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_3000.getValue()) {
                        meetingResourcePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_3000.getCode());
                    }


                    Date date = new Date(item.getExpireDate());
                    meetingResourcePO.setExpireDate(date);
                    System.out.println("打印打印meetingResoucePO:" + meetingResourcePO);
                    meetingResourceDaoService.save(meetingResourcePO);

                }
            }
            System.out.println("打印response:" + response.toString());

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            //System.out.println(e.getHttpStatusCode());
            //System.out.println(e.getRequestId());
            //System.out.println(e.getErrorCode());
            //System.out.println(e.getErrorMsg());
        }
    }

    @Test
    void assignMeetingResource() {
        //更改本地数据库分配会议资源操作
        int i = meetingResourceDaoService.assignMeetingResource("2bca6344aedc444598cdc76a4adc8f22");
        System.out.println("打印打印打印:" + i);
    }


    @Test
    void updateStatusByAccId() {
        meetingResourceMapper.updateStatusByAccId("2bca6344aedc444598cdc76a4adc8f22");
    }

    @Test
    void updateMeetingResourceStatusPrivate() {
        meetingResourceMapper.updateMeetingResourceStatusPrivate("2bca6344aedc444598cdc76a4adc8f22");
    }
}