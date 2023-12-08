package com.tiens.meeting.dubboservice.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.QueryOrgVmrResultDTO;
import com.huaweicloud.sdk.meeting.v1.model.SearchCorpVmrRequest;
import com.huaweicloud.sdk.meeting.v1.model.SearchCorpVmrResponse;
import com.tiens.api.service.RpcMeetingRoomService;
import com.tiens.api.vo.MeetingResouceSelectVO;
import com.tiens.api.vo.VMMeetingCredentialVO;
import com.tiens.meeting.ServiceApplication;
import com.tiens.meeting.repository.mapper.MeetingResouceMapper;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import com.tiens.meeting.repository.service.MeetingResouceDaoService;
import common.enums.MeetingResourceEnum;
import common.enums.MeetingResourceStatusEnum;
import common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceApplication.class)
@ActiveProfiles("local")
class RpcMeetingResouceServiceImplTest {

    @Autowired
    MeetingResouceDaoService meetingResouceDaoService;
    @Autowired
    MeetingResouceMapper meetingResouceMapper;

    @Test
    void getClient() {
        MeetingClient client = SpringUtil.getBean(MeetingClient.class);
        System.out.println("打印:" + client);
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();

        request.withVmrMode(1);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                System.out.println("打印打印打印打印打印打印打印item:" + item);
                String vmrId = item.getId();
                MeetingResouceSelectVO meetingResouceSelectVO = meetingResouceMapper.selectVOByVMR(vmrId);
                if (meetingResouceSelectVO == null) {

                    MeetingResoucePO meetingResoucePO = new MeetingResoucePO();
                    meetingResoucePO.setVmrId(item.getId());
                    meetingResoucePO.setVmrConferenceId(item.getVmrId());
                    meetingResoucePO.setVmrMode(1);
                    meetingResoucePO.setVmrName(item.getVmrName());
                    meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                    meetingResoucePO.setSize(item.getMaxAudienceParties());

                    Integer status = item.getStatus();
                    if (status == 1) {
                        meetingResoucePO.setStatus(MeetingResourceStatusEnum.MEETING_RESOURCE_STATUS_PUBLIC_FREE.getCode());
                    } else if (status == 2) {
                        meetingResoucePO.setStatus(MeetingResourceStatusEnum.MEETING_RESOURCE_STATUS_PUBLIC_RESERVED.getCode());
                    } else if (status == 3) {
                        meetingResoucePO.setStatus(MeetingResourceStatusEnum.MEETING_RESOURCE_STATUS_PRIVATE.getCode());
                    } else if (status == 4) {
                        meetingResoucePO.setStatus(MeetingResourceStatusEnum.MEETING_RESOURCE_STATUS_PUBLIC_PRE_ALLOCATED.getCode());
                    }

                    Integer vmrPkgParties = item.getMaxAudienceParties();
                    if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_10.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_10.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_50.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_50.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_100.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_100.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_200.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_200.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_500.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_500.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_1000.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_1000.getCode());
                    } else if (vmrPkgParties == MeetingResourceEnum.MEETING_RESOURCE_3000.getValue()) {
                        meetingResoucePO.setResourceType(MeetingResourceEnum.MEETING_RESOURCE_3000.getCode());
                    }


                    Date date = new Date(item.getExpireDate());
                    meetingResoucePO.setExpireDate(date);
                    System.out.println("打印打印meetingResoucePO:" + meetingResoucePO);
                    meetingResouceDaoService.save(meetingResoucePO);

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
    void assignMeetingResouce() {
        //更改本地数据库分配会议资源操作
        int i = meetingResouceDaoService.assignMeetingResouce("2bca6344aedc444598cdc76a4adc8f22");
        System.out.println("打印打印打印:" + i);
    }


    @Test
    void updateStatusByAccId() {
        meetingResouceMapper.updateStatusByAccId("2bca6344aedc444598cdc76a4adc8f22");
    }

    @Test
    void updateMeetingResourceStatusPrivate() {
        meetingResouceMapper.updateMeetingResourceStatusPrivate("2bca6344aedc444598cdc76a4adc8f22");
    }
}