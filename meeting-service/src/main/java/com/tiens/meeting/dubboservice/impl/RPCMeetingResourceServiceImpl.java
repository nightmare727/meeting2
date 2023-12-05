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

        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        request.withVmrMode(1);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                MeetingResoucePO meetingResoucePO = new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getVmrId());
                meetingResoucePO.setVmrMode(1);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getVmrPkgParties());

                Integer status = item.getStatus();
                if (status ==0){
                    meetingResoucePO.setStatus("公有空闲");
                }else if (status ==1){
                    meetingResoucePO.setStatus("公有预约");
                }else if (status ==2){
                    meetingResoucePO.setStatus("私有");
                }

                Date date = new Date(item.getExpireDate());
                meetingResoucePO.setExpireDate(date);
                meetingResouceDaoService.insertMeetingResoucePO(meetingResoucePO);

                //meetingResouceDaoService.save(meetingResoucePO);

            }
            System.out.println(response.toString());

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            System.out.println(e.getHttpStatusCode());
            System.out.println(e.getRequestId());
            System.out.println(e.getErrorCode());
            System.out.println(e.getErrorMsg());
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

        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        request.withVmrMode(2);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData) {
                MeetingResoucePO meetingResoucePO = new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getVmrId());
                meetingResoucePO.setVmrMode(2);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getVmrPkgParties());

                Integer status = item.getStatus();
                if (status ==0){
                    meetingResoucePO.setStatus("公有空闲");
                }else if (status ==1){
                    meetingResoucePO.setStatus("公有预约");
                }else if (status ==2){
                    meetingResoucePO.setStatus("私有");
                }

                Date date = new Date(item.getExpireDate());
                meetingResoucePO.setExpireDate(date);
                meetingResouceDaoService.insertMeetingResoucePO(meetingResoucePO);

            }
            System.out.println(response.toString());

        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (ServiceResponseException e) {
            e.printStackTrace();
            System.out.println(e.getHttpStatusCode());
            System.out.println(e.getRequestId());
            System.out.println(e.getErrorCode());
            System.out.println(e.getErrorMsg());
        }

    }

    /**
     * 更改会议资源状态:置为空闲
     *
     * @param vmrId
     * @return
     */
    @Override
    public CommonResult updateMeetingStatus(String vmrId) throws ServiceException {
        int a = meetingResouceDaoService.updateMeetingStatusById(vmrId);

        //通过vmrid查询accid
        String accId = meetingResouceDaoService.selectAccIdByVmrId(vmrId);

        System.out.println("更新结果为:" + a);
        boolean result = false;
        try {
            result = disassociateVmr(accId);
        } catch (Exception e) {
            log.error("添加普通用户发生异常！", e);
        }
        return CommonResult.success(result);
    }

    private boolean disassociateVmr(String accId) throws ServiceException {
//        AssociateVmrRequest request = new AssociateVmrRequest();
//        MeetingUserAccidDTO body = new MeetingUserAccidDTO();
//        body.setAccId(accId);
//        request.withBody(body);
//
//        /*MeetingClient client = SpringUtil.getBean(MeetingClient.class);
//        AssociateVmrResponse associateVmrResponse = client.associateVmr(request);
//        System.out.println("associateVmrResponse"+associateVmrResponse);*/
//        try {
//            MeetingClient meetingClient = SpringUtil.getBean(MeetingClient.class);
//            AddUserResponse response = meetingClient.disassociateVmr(request);
//            log.info("华为云添加用户结果：{}", JSON.toJSONString(response));
//        } catch (ConnectionException e) {
//            e.printStackTrace();
//        } catch (RequestTimeoutException e) {
//            e.printStackTrace();
//        } catch (ServiceResponseException e) {
//            e.printStackTrace();
//            System.out.println(e.getHttpStatusCode());
//            System.out.println(e.getRequestId());
//            System.out.println(e.getErrorCode());
//            System.out.println(e.getErrorMsg());
//        }
        return true;
    }

    /**
     * 分配会议资源
     *
     * @param meetingResouceIdDTO
     * @return
     */
    @Override
    public CommonResult assignMeetingResouce(MeetingResouceIdDTO meetingResouceIdDTO) throws ServiceException {
        int b = meetingResouceDaoService.assignMeetingResouce(meetingResouceIdDTO);
        return CommonResult.success(b);
    }

    /**
     * 查询主持人
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
