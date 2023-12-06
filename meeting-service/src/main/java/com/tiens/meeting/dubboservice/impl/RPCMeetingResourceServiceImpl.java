package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.MeetingCredentials;
import com.huaweicloud.sdk.meeting.v1.model.*;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.dto.MeetingResoucePageDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResouceVO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.dubboservice.config.HWMeetingConfiguration;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import com.tiens.meeting.repository.service.MeetingResouceDaoService;
import common.enums.MeetingResourceEnum;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/23
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class RPCMeetingResourceServiceImpl implements RPCMeetingResourceService {


    public static void main(String[] args) {
        ZoneId zoneId1 = ZoneId.of("GMT+09:00");
        ZoneId zoneId2 = ZoneId.of("GMT+07:00");
        Instant now = Instant.now();
        Date date = new Date();


        DateTime dateTime = DateUtil.convertTimeZone(date, zoneId1);

        System.out.println(dateTime);
//        ZonedDateTime zonedDateTime = now.atZone(zoneId1);
//        System.out.println(zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

//        System.out.println(now);
//        System.out.println(zonedDateTime);
    }


    private final MeetingResouceDaoService meetingResouceDaoService;

    private final MeetingConfig meetingConfig;

    @Override
    public CommonResult<MeetingResouceVO> queryMeetingResouce(String vmrId) throws ServiceException {
        return null;
    }

    /**
     * 会议资源列表展示
     *
     * @param
     * @return
     */
    @Override
    public PageResult<MeetingResouceVO> queryMeetingResoucePage(PageParam<MeetingResoucePageDTO> pageDTOPageParam) throws ServiceException {
        Page<MeetingResoucePO> page = new Page<>(pageDTOPageParam.getPageNum(), pageDTOPageParam.getPageSize());
        /*查询条件
        MeetingResoucePageDTO condition = pageDTOPageParam.getCondition();
        LambdaQueryWrapper<MeetingResoucePO> queryWrapper = Wrappers.lambdaQuery(MeetingResoucePO.class);
                .like(ObjectUtil.isNotEmpty(condition.getName()), MeetingResoucePO::getName, condition.getName())
                .eq(ObjectUtil.isNotEmpty(condition.getJoyoCode()), MeetingResoucePO::getJoyoCode, condition.getJoyoCode())
                .like(ObjectUtil.isNotEmpty(condition.getPhone()), MeetingResoucePO::getPhone, condition.getPhone())
                .like(ObjectUtil.isNotEmpty(condition.getEmail()), MeetingResoucePO::getEmail, condition.getEmail())
                .orderByDesc(MeetingResoucePO::getCreateTime);*/
        Page<MeetingResoucePO> pagePoResult = meetingResouceDaoService.page(page);
        List<MeetingResoucePO> records = pagePoResult.getRecords();
        List<MeetingResouceVO> meetingResouceVOS = BeanUtil.copyToList(records, MeetingResouceVO.class);
        PageResult<MeetingResouceVO> pageResult = new PageResult<>();
        pageResult.setList(meetingResouceVOS);
        pageResult.setTotal(pagePoResult.getTotal());
        return pageResult;
    }

    /**
     * 调取华为会议资源:1云会议室
     *
     * @param
     * @return
     */
    @Override
    public void SearchCorpVmrSolution1() throws ServiceException {
        MeetingClient client = SpringUtil.getBean(MeetingClient.class);
        System.out.println("打印:" + client);
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();

        request.withVmrMode(1);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                System.out.println("打印打印打印打印打印打印打印item:" + item);
                MeetingResoucePO meetingResoucePO = new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getId());
                meetingResoucePO.setVmrConferenceId(item.getVmrId());
                meetingResoucePO.setVmrMode(1);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getMaxAudienceParties());

                Integer status = item.getStatus();
                if (status == 0) {
                    meetingResoucePO.setStatus("公有空闲");
                } else if (status == 1) {
                    meetingResoucePO.setStatus("公有预约");
                } else if (status == 2) {
                    meetingResoucePO.setStatus("私有");
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
                meetingResouceDaoService.save(meetingResoucePO);

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


    /**
     * 调取华为会议资源:2网络研讨会
     *
     * @param
     * @return
     */
    @Override
    public void SearchCorpVmrSolution2() throws ServiceException {
        MeetingClient client = SpringUtil.getBean(MeetingClient.class);
        System.out.println("打印:" + client);
        SearchCorpVmrRequest request = new SearchCorpVmrRequest();

        request.withVmrMode(2);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                System.out.println("打印打印打印打印打印打印打印item:" + item);
                MeetingResoucePO meetingResoucePO = new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getId());
                meetingResoucePO.setVmrConferenceId(item.getVmrId());
                meetingResoucePO.setVmrMode(2);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getMaxAudienceParties());

                Integer status = item.getStatus();
                if (status == 0) {
                    meetingResoucePO.setStatus("公有空闲");
                } else if (status == 1) {
                    meetingResoucePO.setStatus("公有预约");
                } else if (status == 2) {
                    meetingResoucePO.setStatus("私有");
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
                meetingResouceDaoService.save(meetingResoucePO);

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


    /**
     * 更改会议资源状态:取消分配,操作后，此资源变为公有。
     *
     * @param vmrId
     * @return
     */
    @Override
    public CommonResult updateMeetingStatus(String vmrId) throws ServiceException {
        //更改本地数据库状态
        int a = meetingResouceDaoService.updateMeetingStatusById(vmrId);
        System.out.println("打印本地数据库资源状态更改结果:" + a);
        //同步到华为云
        //1:通过vmrid查询accid
        //若此资源下 有原来的预约会议，则为公有预约。不取消其在私有状态下的会议。
        //判断查询一下名下是否有会议根据conferenceID查询ShowMeetingDetail(查询会议详情)
        String accId = meetingResouceDaoService.selectAccIdByVmrId(vmrId);
        System.out.println("打印查询到的accid为:" + accId);
        boolean result = false;
        try {
            result = disassociateVmr(accId);
        } catch (Exception e) {
            log.error("取消分配操作发生异常！", e);
        }
        return CommonResult.success(result);
    }

    //同步华为云回收云会议室
    private boolean disassociateVmr(String accId) throws ServiceException {
        DisassociateVmrRequest request = new DisassociateVmrRequest();
        request.withAccount(accId);
        try {
            MeetingClient client = SpringUtil.getBean(MeetingClient.class);
            System.out.println("打印:" + client);
            DisassociateVmrResponse response = client.disassociateVmr(request);
            System.out.println(response.toString());
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            System.out.println(e.getHttpStatusCode());
            System.out.println(e.getRequestId());
            System.out.println(e.getErrorCode());
            System.out.println(e.getErrorMsg());
        }
        return true;
    }

    /**
     * 分配会议资源
     * 公有空闲状态 即 公有资源 无人预约时
     *  可进行 分配操作,分配后
     *  此资源变为私有状态
     * @param joyoCode
     * @return
     */
    @Override
    public CommonResult assignMeetingResouce(String joyoCode) throws ServiceException {
        //int b = meetingResouceDaoService.assignMeetingResouce(joyoCode);
        //System.out.println("分配会议资源更改数据库结果:" + b);

        //查询一下传入的joyocode号所对应的accid
        String accId = meetingResouceDaoService.seleceAccIdByJoyoCode(joyoCode);
        //更改本地数据库分配会议资源操作
        int i = meetingResouceDaoService.assignMeetingResouce(accId);
        System.out.println("打印分配会议资源更改数据库结果:" + i);

        System.out.println("打印查询到的accid为:" + accId);

        //判断华为云同步结果
        boolean result = false;
        try {
            result = associateVmrSolution(accId);
        } catch (Exception e) {
            log.error("分配会议资源操作发生异常！", e);
        }
        return CommonResult.success(result);
    }

    //同步华为云分配会议室
    private boolean associateVmrSolution(String accId) throws ServiceException {
        AssociateVmrRequest request = new AssociateVmrRequest();
        request.withAccount(accId);
        try {
            MeetingClient client = SpringUtil.getBean(MeetingClient.class);
            AssociateVmrResponse response = client.associateVmr(request);
            System.out.println(response.toString());
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (RequestTimeoutException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            System.out.println(e.getHttpStatusCode());
            System.out.println(e.getRequestId());
            System.out.println(e.getErrorCode());
            System.out.println(e.getErrorMsg());
        }
        return true;
    }


    /**
     * 查询joyocode
     *
     * @param joyoCode
     * @return
     */
    @Override
    public MeetingHostUserVO selectUserByJoyoCode(String joyoCode) {
        MeetingHostUserVO meetingHostUserVO = meetingResouceDaoService.selectUserByJoyoCode(joyoCode);
        return meetingHostUserVO;
    }
}
