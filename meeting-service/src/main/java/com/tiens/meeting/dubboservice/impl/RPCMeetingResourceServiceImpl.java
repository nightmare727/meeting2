package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiens.api.dto.MeetingHostPageDTO;
import com.tiens.api.dto.MeetingResouceIdDTO;
import com.tiens.api.dto.MeetingResoucePageDTO;
import com.tiens.api.service.RPCMeetingResourceService;
import com.tiens.api.vo.MeetingHostUserVO;
import com.tiens.api.vo.MeetingResouceVO;
import com.tiens.meeting.repository.po.MeetingHostUserPO;
import com.tiens.meeting.repository.po.MeetingResoucePO;
import com.tiens.meeting.repository.service.MeetingResouceDaoService;
import common.exception.ServiceException;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
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

        System.out.println(now);
//        System.out.println(zonedDateTime);
    }

    @Autowired
    private final MeetingResouceDaoService meetingResouceDaoService;
    @Autowired
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

        /*String userName = "<YOUR USER NAME>";
        String password = "<YOUR PASSWORD>";
        String endpoint = "https://api.meeting.huaweicloud.com";

        ICredential auth = new MeetingCredentials()
                .withUserName(userName)
                .withUserPassword(password);

        MeetingClient client = MeetingClient.newBuilder()
                .withCredential(auth)
                .withEndpoint(endpoint)
                .build();*/

        HWMeetingConfiguration hwMeetingConfiguration = new HWMeetingConfiguration();
        MeetingClient client = hwMeetingConfiguration.meetingClient(meetingConfig);

        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        request.withVmrMode(1);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData){
                MeetingResoucePO meetingResoucePO=new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getVmrId());
                meetingResoucePO.setVmrMode(1);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getVmrPkgParties());
                meetingResoucePO.setStatus(item.getStatus());
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
     * 调取华为会议资源:2网络研讨会
     * @param
     * @return
     */
    @Override
    public void SearchCorpVmrSolution2() throws ServiceException {
        /*String userName = "<YOUR USER NAME>";
        String password = "<YOUR PASSWORD>";
        String endpoint = "https://api.meeting.huaweicloud.com";

        ICredential auth = new MeetingCredentials()
                .withUserName(userName)
                .withUserPassword(password);

        MeetingClient client = MeetingClient.newBuilder()
                .withCredential(auth)
                .withEndpoint(endpoint)
                .build();*/

        HWMeetingConfiguration hwMeetingConfiguration = new HWMeetingConfiguration();
        MeetingClient client = hwMeetingConfiguration.meetingClient(meetingConfig);

        SearchCorpVmrRequest request = new SearchCorpVmrRequest();
        request.withVmrMode(2);
        try {
            SearchCorpVmrResponse response = client.searchCorpVmr(request);
            List<QueryOrgVmrResultDTO> responseData = response.getData();
            for (QueryOrgVmrResultDTO item : responseData){
                MeetingResoucePO meetingResoucePO=new MeetingResoucePO();
                meetingResoucePO.setVmrId(item.getVmrId());
                meetingResoucePO.setVmrMode(2);
                meetingResoucePO.setVmrName(item.getVmrName());
                meetingResoucePO.setVmrPkgName(item.getVmrPkgName());
                meetingResoucePO.setSize(item.getVmrPkgParties());
                meetingResoucePO.setStatus(item.getStatus());
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
        return CommonResult.success(a);
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
