package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.QueryOrgVmrResultDTO;
import com.huaweicloud.sdk.meeting.v1.model.SearchCorpVmrRequest;
import com.huaweicloud.sdk.meeting.v1.model.SearchCorpVmrResponse;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.repository.po.MeetingResourcePO;
import com.tiens.meeting.repository.service.MeetingResourceDaoService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import common.enums.MeetingResourceEnum;
import common.enums.MeetingResourceStateEnum;
import common.enums.MeetingRoomHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/5
 * @Version 1.0
 * @Company: tiens 华为资源同步
 */
@Component
@Slf4j
public class HWResourceTask {

    @Resource
    RPCMeetingResourceService rpcMeetingResourceService;

    @Resource
    MeetingResourceDaoService meetingResourceDaoService;

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @XxlJob("HWResourceJobHandler")
    @MDCLog
    public void jobHandler() throws Exception {
        List<MeetingResourcePO> hwResourceList = getHwResourceList();
        List<MeetingResourcePO> oldResourceList = meetingResourceDaoService.list();

        List<String> hwResourceIds =
            hwResourceList.stream().map(MeetingResourcePO::getVmrId).collect(Collectors.toList());
        List<String> oldResourceIds =
            oldResourceList.stream().map(MeetingResourcePO::getVmrId).collect(Collectors.toList());

        List<String> newResourceIds = CollectionUtil.subtractToList(hwResourceIds, oldResourceIds);

        List<MeetingResourcePO> collect =
            hwResourceList.stream().filter(r -> newResourceIds.contains(r.getVmrId())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(collect)) {
            meetingResourceDaoService.saveBatch(collect);
        }
        log.info("【定时执行华为资源同步】，共导入数量：{}条", collect.size());
    }

    private List<MeetingResourcePO> getHwResourceList() {

        ArrayList<@Nullable MeetingResourcePO> results = Lists.newArrayList();

        //1、获取华为云资源
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        //1:云会议室资源列表
        request.withVmrMode(MeetingRoomHandlerEnum.CLOUD.getVmrMode());
        request.withLimit(100);
        MeetingClient mgrMeetingClient = hwMeetingCommonService.getMgrMeetingClient();
        SearchCorpVmrResponse response1 = mgrMeetingClient.searchCorpVmr(request);
        List<QueryOrgVmrResultDTO> data1 = response1.getData();
        if (CollectionUtil.isNotEmpty(data1)) {
            results.addAll(
                data1.stream().map(t -> this.convert(t, MeetingRoomHandlerEnum.CLOUD)).collect(Collectors.toList()));
        }

        //2、研讨会资源
        request.withVmrMode(MeetingRoomHandlerEnum.SEMINAR.getVmrMode());
        SearchCorpVmrResponse response2 = mgrMeetingClient.searchCorpVmr(request);
        List<QueryOrgVmrResultDTO> data2 = response2.getData();
        if (CollectionUtil.isNotEmpty(data2)) {
            results.addAll(
                data2.stream().map(t -> this.convert(t, MeetingRoomHandlerEnum.SEMINAR)).collect(Collectors.toList()));
        }
        return results;
    }

    MeetingResourcePO convert(QueryOrgVmrResultDTO queryOrgVmrResultDTO,
        MeetingRoomHandlerEnum meetingRoomHandlerEnum) {

        Integer maxSize =
            NumberUtil.max(queryOrgVmrResultDTO.getVmrPkgParties(), queryOrgVmrResultDTO.getMaxAudienceParties());
        MeetingResourcePO meetingResourcePO = new MeetingResourcePO();
        meetingResourcePO.setVmrId(queryOrgVmrResultDTO.getId());
        meetingResourcePO.setVmrConferenceId(queryOrgVmrResultDTO.getVmrId());
        meetingResourcePO.setVmrMode(meetingRoomHandlerEnum.getVmrMode());
        meetingResourcePO.setVmrName(queryOrgVmrResultDTO.getVmrName());
        meetingResourcePO.setVmrPkgName(queryOrgVmrResultDTO.getVmrPkgName());
        meetingResourcePO.setSize(maxSize);
        meetingResourcePO.setStatus(MeetingResourceStateEnum.PUBLIC_FREE.getState());
        meetingResourcePO.setExpireDate(DateUtil.date(queryOrgVmrResultDTO.getExpireDate()));
//        meetingResourcePO.setOwnerImUserId();
        meetingResourcePO.setResourceType(MeetingResourceEnum.getBySize(maxSize).getCode());

        return meetingResourcePO;
    }
}
