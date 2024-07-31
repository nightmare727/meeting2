package com.tiens.meeting.dubboservice.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
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
import java.util.*;
import java.util.function.Function;
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
    @MDCLog(description = "定时执行华为资源同步")
    public void jobHandler() throws Exception {
        List<MeetingResourcePO> hwResourceList = getHwResourceList();
        Map<String, MeetingResourcePO> hwResourceMap =
            hwResourceList.stream().collect(Collectors.toMap(MeetingResourcePO::getVmrId, Function.identity()));

        List<MeetingResourcePO> oldResourceList =
            meetingResourceDaoService.lambdaQuery().orderByAsc(MeetingResourcePO::getSize).list();

        Map<String, MeetingResourcePO> oldResourceMap =
            oldResourceList.stream().collect(Collectors.toMap(MeetingResourcePO::getVmrId, Function.identity()));

        //新资源列表
        List<MeetingResourcePO> newResources = CollectionUtil.subtractToList(hwResourceList, oldResourceList);
        if (CollectionUtil.isNotEmpty(newResources)) {
            meetingResourceDaoService.saveBatch(newResources);
            log.info("【定时执行华为资源同步】，共导入数量：{}条", newResources.size());
        }
        //已过期或者不存在的资源
        List<MeetingResourcePO> invalidResources = CollectionUtil.subtractToList(oldResourceList, hwResourceList);
        if (CollectionUtil.isNotEmpty(invalidResources)) {
            log.info("【定时执行华为资源同步】 删除过期资源 ，data:{}", JSON.toJSONString(invalidResources));
            List<Integer> deleteResourceIds =
                invalidResources.stream().map(MeetingResourcePO::getId).collect(Collectors.toList());
            meetingResourceDaoService.removeBatchByIds(deleteResourceIds);
        }
        Collection<MeetingResourcePO> intersection = CollectionUtil.intersection(oldResourceList, hwResourceList);
        //处理更新资源更新时间
        if (CollectionUtil.isNotEmpty(intersection)) {
            for (MeetingResourcePO meetingResourcePO : intersection) {
                String vmrId = meetingResourcePO.getVmrId();
                Date oldExpireDate = oldResourceMap.get(vmrId).getExpireDate();
                Date hwExpireDate = hwResourceMap.get(vmrId).getExpireDate();
                if (!hwExpireDate.equals(oldExpireDate)) {
                    //更新过期时间
                    boolean update = meetingResourceDaoService.lambdaUpdate().eq(MeetingResourcePO::getVmrId, vmrId)
                        .set(MeetingResourcePO::getExpireDate, hwExpireDate).update();
                    log.info("【定时执行华为资源同步】 更新资源过期时间完成，vmrId：{}，过期时间：{},执行结果：{}", vmrId,
                        hwExpireDate, update);
                }
            }
        }
    }

    private List<MeetingResourcePO> getHwResourceList() {

        ArrayList<@Nullable MeetingResourcePO> results = Lists.newArrayList();

        //1、获取华为云资源
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        //1:云会议室资源列表
        request.withVmrMode(MeetingRoomHandlerEnum.CLOUD.getVmrMode());
        request.withLimit(100);
        MeetingClient mgrMeetingClient = hwMeetingCommonService.getMgrMeetingClient();
        Boolean finished = true;
        int offset = 0;
        do {
            SearchCorpVmrResponse response1 = mgrMeetingClient.searchCorpVmr(request);
            request.withOffset(offset * 100);

            Integer count = response1.getCount();

            List<QueryOrgVmrResultDTO> data1 = response1.getData();

            if (CollectionUtil.isNotEmpty(data1)) {
                if (data1.size() == count) {
                    finished = false;
                } else if (data1.size() < 100) {
                    finished = false;
                } else {
                    offset++;
                }
                results.addAll(
                    data1.stream().map(t -> this.convert(t, MeetingRoomHandlerEnum.CLOUD)).filter(ObjectUtil::isNotNull)
                        .collect(Collectors.toList()));
            }
        } while (finished);

        //2、研讨会资源
        request.withVmrMode(MeetingRoomHandlerEnum.SEMINAR.getVmrMode());
        SearchCorpVmrResponse response2 = mgrMeetingClient.searchCorpVmr(request);
        List<QueryOrgVmrResultDTO> data2 = response2.getData();
        if (CollectionUtil.isNotEmpty(data2)) {
            results.addAll(
                data2.stream().map(t -> this.convert(t, MeetingRoomHandlerEnum.SEMINAR)).filter(ObjectUtil::isNotNull)
                    .collect(Collectors.toList()));
        }
        return results;
    }

    MeetingResourcePO convert(QueryOrgVmrResultDTO queryOrgVmrResultDTO,
        MeetingRoomHandlerEnum meetingRoomHandlerEnum) {
        if (ObjectUtil.isNull(queryOrgVmrResultDTO.getExpireDate())) {
            //过期时间为空，则资源无效
            return null;

        }

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
