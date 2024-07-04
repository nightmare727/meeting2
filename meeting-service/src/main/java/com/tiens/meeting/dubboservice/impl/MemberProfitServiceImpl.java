package com.tiens.meeting.dubboservice.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.api.dto.CmsShowGetDTO;
import com.tiens.api.dto.MeetingRoomContextDTO;
import com.tiens.api.dto.PushOrderDTO;
import com.tiens.api.service.MemberProfitService;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.CmsShowVO;
import com.tiens.api.vo.MeetingBlackUserVO;
import com.tiens.api.vo.MeetingUserProfitVO;
import com.tiens.api.vo.VMUserVO;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.repository.po.MeetingBlackUserPO;
import com.tiens.meeting.repository.po.MeetingUserProfitOrderPO;
import com.tiens.meeting.repository.service.MeetingBlackUserDaoService;
import com.tiens.meeting.repository.service.MeetingUserProfitOrderDaoService;
import common.enums.TerminalEnum;
import common.pojo.CommonResult;
import common.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MemberProfitServiceImpl implements MemberProfitService {

    private final MeetingConfig meetingConfig;

    private final MeetingBlackUserDaoService meetingBlackUserDaoService;

    private final MeetingUserProfitOrderDaoService meetingUserProfitOrderDaoService;


    @Autowired
    RpcMeetingUserService rpcMeetingUserService;

    /**
     * 校验用户权益
     *
     * @param meetingRoomContextDTO
     * @return
     */
    @Override
    public CommonResult checkProfit(MeetingRoomContextDTO meetingRoomContextDTO) {
        return null;
    }

    /**
     * 查询首页头图展示
     *
     * @return
     */
    @Override
    public CommonResult<CmsShowVO> getCmsShow(CmsShowGetDTO cmsShowGetDTO) {
        MeetingConfig.CmsShowConfigInner cmsShowConfig = meetingConfig.getCmsShowConfig();
        if (!cmsShowConfig.getEnable()) {
            return CommonResult.success(null);
        }
        Integer deviceType = cmsShowGetDTO.getDeviceType();
        TerminalEnum byTerminal = TerminalEnum.getByTerminal(deviceType);

        String deviceSuggestion = null;
        CmsShowVO cmsShowVO = new CmsShowVO();

        switch (byTerminal) {
            case ANDROID:
                deviceSuggestion = cmsShowConfig.getAndroidBaseConfig();
                break;
            case IOS:
                deviceSuggestion = cmsShowConfig.getIosBaseConfig();
                break;
            case WINDOWS:
                deviceSuggestion = cmsShowConfig.getWindowsBaseConfig();
                break;
            case MAC:
                deviceSuggestion = cmsShowConfig.getMacBaseConfig();
                break;

        }
        cmsShowVO.setDeviceSuggestion(deviceSuggestion);
        return CommonResult.success(cmsShowVO);
    }

    /**
     * 推送订单
     *
     * @param pushOrderDTO
     * @return
     */
    @Override
    public CommonResult pushOrder(PushOrderDTO pushOrderDTO) {

        log.error("订单推送入参，订单数据：{}", JSON.toJSONString(pushOrderDTO));
        CommonResult<VMUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryVMUser(pushOrderDTO.getJoyoCode(), "");
        VMUserVO data = vmUserVOCommonResult.getData();
        if (ObjectUtil.isNull(data)) {
            log.error("【订单推送】查询用户异常，data：{}", pushOrderDTO);
        }
        //存储订单数据
        MeetingUserProfitOrderPO meetingUserProfitOrderPO = new MeetingUserProfitOrderPO();
        meetingUserProfitOrderPO.setUserId(data.getAccid());
        meetingUserProfitOrderPO.setJoyoCode(data.getJoyoCode());
        meetingUserProfitOrderPO.setOrderNo(pushOrderDTO.getOrderNo());
        meetingUserProfitOrderPO.setSkuId(pushOrderDTO.getSkuId());
        meetingUserProfitOrderPO.setOrderStatus(pushOrderDTO.getOrderStatus());
        meetingUserProfitOrderPO.setPaidAmount(pushOrderDTO.getPaidVmAmount());
        meetingUserProfitOrderPO.setResourceType(pushOrderDTO.getResourceType());
        //        meetingUserProfitOrderPO.setResourceDesc();
        meetingUserProfitOrderPO.setDuration(pushOrderDTO.getDuration());

        try {
            boolean save = meetingUserProfitOrderDaoService.save(meetingUserProfitOrderPO);
        } catch (DuplicateKeyException e) {
            log.error("订单重复录入异常，订单数据：{}", JSON.toJSONString(pushOrderDTO));
        }


        return CommonResult.success(null);
    }

    /**
     * 查询黑名单用户
     *
     * @param finalUserId
     * @return
     */
    @Override
    public CommonResult<MeetingBlackUserVO> getBlackUser(String finalUserId) {
        DateTime now = DateUtil.convertTimeZone(DateUtil.date(), DateUtils.TIME_ZONE_GMT);

        //校验黑名单
        List<MeetingBlackUserPO> blackUserPOList = meetingBlackUserDaoService.lambdaQuery()
                .eq(MeetingBlackUserPO::getUserId, finalUserId)
                .le(MeetingBlackUserPO::getStartTime, now).ge(MeetingBlackUserPO::getEndTime, now).list();

        if (ObjectUtil.isNotEmpty(blackUserPOList)) {
            MeetingBlackUserPO meetingBlackUserPO = blackUserPOList.get(0);
            MeetingBlackUserVO meetingBlackUserVO =
                    BeanUtil.copyProperties(meetingBlackUserPO, MeetingBlackUserVO.class);
            return CommonResult.success(meetingBlackUserVO);

        }
        return CommonResult.success(null);
    }

    /**
     * 查询用户权益
     *
     * @param finalUserId
     * @return
     */
    @Override
    public CommonResult<MeetingUserProfitVO> getUserProfit(String finalUserId) {

        return null;
    }
}
