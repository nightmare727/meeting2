package com.tiens.meeting.dubboservice.impl;

import com.google.common.collect.Lists;
import com.tiens.api.dto.ResourceAllocateDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.repository.po.MeetingTimeZoneConfigPO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.repository.service.MeetingTimeZoneConfigDaoService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class RpcMeetingResourceServiceImplTest {

    @Autowired
    RPCMeetingResourceService rpcMeetingResourceService;


    @Autowired
    MeetingTimeZoneConfigDaoService meetingTimeZoneConfigDaoService;
    /*@Test
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
    }*/

    @Test
    void allocate() {
        ResourceAllocateDTO resourceAllocateDTO = new ResourceAllocateDTO();
        resourceAllocateDTO.setJoyoCode("67891676");
        resourceAllocateDTO.setResourceId(227);

        System.out.println(rpcMeetingResourceService.allocate(resourceAllocateDTO));

    }
    @Test
    void getId() {
        MeetingTimeZoneConfigPO meetingTimeZoneConfigPO = new MeetingTimeZoneConfigPO();
        meetingTimeZoneConfigPO.setChineseDesc("asa");
        meetingTimeZoneConfigPO.setEnglishDesc("asa");
        meetingTimeZoneConfigPO.setTimeZoneId(1);
        meetingTimeZoneConfigPO.setTimeZoneOffset("12313");
        ArrayList< MeetingTimeZoneConfigPO> objects = Lists.newArrayList();
        objects.add(meetingTimeZoneConfigPO);
        boolean save = meetingTimeZoneConfigDaoService.saveBatch(objects);
        System.out.println(meetingTimeZoneConfigPO);

    }

}