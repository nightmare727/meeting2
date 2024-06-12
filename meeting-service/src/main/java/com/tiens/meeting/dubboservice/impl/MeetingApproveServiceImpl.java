package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiens.api.dto.MeetingApproveDTO;
import com.tiens.api.dto.MeetingApproveOperateDTO;
import com.tiens.api.dto.MeetingApprovePageDTO;
import com.tiens.api.service.MeetingApproveService;
import com.tiens.api.vo.MeetingApproveVO;
import com.tiens.meeting.repository.po.MeetingApprovePO;
import com.tiens.meeting.repository.service.MeetingApproveDaoService;
import common.enums.MeetingApproveStateEnum;
import common.exception.enums.GlobalErrorCodeConstants;
import common.pojo.CommonResult;
import common.pojo.PageParam;
import common.pojo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/6/12
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MeetingApproveServiceImpl implements MeetingApproveService {

    private final MeetingApproveDaoService meetingApproveDaoService;

    /**
     * 保存审批
     *
     * @param meetingApproveDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult saveApprove(MeetingApproveDTO meetingApproveDTO) {
        log.info("【大型会议申请】 入参：{}", JSON.toJSONString(meetingApproveDTO));
        //同一个人只能有一条申请中得数据
        Long count = meetingApproveDaoService.lambdaQuery()
            .eq(MeetingApprovePO::getApproveStatus, MeetingApproveStateEnum.APPLYING.getState())
            .eq(MeetingApprovePO::getJoyoCode, meetingApproveDTO.getJoyoCode()).count();
        if (count > 0) {
            return CommonResult.error(GlobalErrorCodeConstants.TOO_MANY_APPLYING_APPROVE);
        }
        MeetingApprovePO meetingApprovePO = BeanUtil.copyProperties(meetingApproveDTO, MeetingApprovePO.class);
        meetingApproveDaoService.save(meetingApprovePO);
        return CommonResult.success(null);
    }

    /**
     * 查询审批列表
     *
     * @param pageDTOPageParam
     * @return
     */
    @Override
    public CommonResult<PageResult<MeetingApproveVO>> getApproveList(
        PageParam<MeetingApprovePageDTO> pageDTOPageParam) {

        Page<MeetingApprovePO> meetingApprovePOPage =
            new Page<>(pageDTOPageParam.getPageNum(), pageDTOPageParam.getPageSize());

        MeetingApprovePageDTO condition = pageDTOPageParam.getCondition();

        Wrapper<MeetingApprovePO> wrapper = Wrappers.lambdaQuery(MeetingApprovePO.class)
            .eq(StrUtil.isNotBlank(condition.getJoyoCode()), MeetingApprovePO::getJoyoCode, condition.getJoyoCode())
            .like(StrUtil.isNotBlank(condition.getPhoneNum()), MeetingApprovePO::getPhoneNum, condition.getPhoneNum())
            .like(StrUtil.isNotBlank(condition.getEmail()), MeetingApprovePO::getEmail, condition.getEmail())
            .eq(ObjectUtil.isNotNull(condition.getApproveStatus()), MeetingApprovePO::getApproveStatus,
                condition.getApproveStatus())
            .eq(ObjectUtil.isNotNull(condition.getResourceArea()), MeetingApprovePO::getResourceArea,
                condition.getResourceArea())
            .orderByDesc(MeetingApprovePO::getCreateTime);

        Page<MeetingApprovePO> page = meetingApproveDaoService.page(meetingApprovePOPage, wrapper);

        List<MeetingApprovePO> records = page.getRecords();
        List<MeetingApproveVO> meetingApproveVOList = BeanUtil.copyToList(records, MeetingApproveVO.class);

        PageResult<MeetingApproveVO> pageResult = new PageResult<>();
        pageResult.setList(meetingApproveVOList);
        pageResult.setTotal(page.getTotal());

        return CommonResult.success(pageResult);
    }

    /**
     * 操作审批
     *
     * @param meetingApproveOperateDTO
     * @return
     */
    @Override
    @Transactional
    public CommonResult approveOperate(MeetingApproveOperateDTO meetingApproveOperateDTO) {

        meetingApproveDaoService.lambdaUpdate().eq(MeetingApprovePO::getId, meetingApproveOperateDTO.getId())
            .set(MeetingApprovePO::getApproveStatus, meetingApproveOperateDTO.getApproveStatus())
            .set(MeetingApprovePO::getApproveRemark, meetingApproveOperateDTO.getApproveRemark()).update();

        return CommonResult.success(null);
    }
}
