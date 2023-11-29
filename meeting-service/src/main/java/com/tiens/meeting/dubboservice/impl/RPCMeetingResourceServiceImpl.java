package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tiens.api.service.RPCMeetingResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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

    private final MeetingResouceDaoService meetingResouceDaoService;

    @Override
    public CommonResult<MeetingResouceVO> queryMeetingResouce(String vmrId) throws ServiceException {
        return null;
    }

    @Override
    public PageResult<MeetingResouceVO> queryMeetingResoucePage(PageParam<MeetingResoucePageDTO> pageDTOPageParam) throws ServiceException {
        /*Page<MeetingResoucePO> page = new Page<>(pageDTOPageParam.getPageNum(), pageDTOPageParam.getPageSize());
        MeetingResoucePageDTO condition = pageDTOPageParam.getCondition();

        LambdaQueryWrapper<MeetingResoucePO> queryWrapper = Wrappers.lambdaQuery(MeetingResoucePO.class)
                .like(ObjectUtil.isNotEmpty(condition.getName()), MeetingResoucePO::getName, condition.getName())
                .eq(ObjectUtil.isNotEmpty(condition.getJoyoCode()), MeetingResoucePO::getJoyoCode, condition.getJoyoCode())
                .like(ObjectUtil.isNotEmpty(condition.getPhone()), MeetingResoucePO::getPhone, condition.getPhone())
                .like(ObjectUtil.isNotEmpty(condition.getEmail()), MeetingResoucePO::getEmail, condition.getEmail())
                .orderByDesc(MeetingResoucePO::getCreateTime);
        Page<MeetingResoucePO> pagePoResult = meetingResouceDaoService.page(page, queryWrapper);
        List<MeetingResoucePO> records = pagePoResult.getRecords();
        List<MeetingResouceVO> meetingHostUserVOS = BeanUtil.copyToList(records, MeetingResouceVO.class);
        PageResult<MeetingResouceVO> pageResult = new PageResult<>();
        pageResult.setList(meetingHostUserVOS);
        pageResult.setTotal(pagePoResult.getTotal());
        return pageResult;*/
        return null;
    }

    @Override
    public CommonResult updateMeetingStatus(String vmrId) throws ServiceException {
        int a = meetingResouceDaoService.updateMeetingStatusById(vmrId);
        return CommonResult.success(a);
    }

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
