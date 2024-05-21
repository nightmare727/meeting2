package com.tiens.meeting.dubboservice.job;

import com.huaweicloud.sdk.meeting.v1.MeetingClient;
import com.huaweicloud.sdk.meeting.v1.model.SearchUsersRequest;
import com.huaweicloud.sdk.meeting.v1.model.SearchUsersResponse;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.dubboservice.core.HwMeetingCommonService;
import com.tiens.meeting.util.mdc.MDCLog;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @Author: 蔚文杰
 * @Date: 2024/5/20
 * @Version 1.0
 * @Company: tiens
 */
@Component
@Slf4j
public class HWUserCleanTask {

    @Autowired
    HwMeetingCommonService hwMeetingCommonService;

    @Autowired
    MeetingConfig meetingConfig;

    @XxlJob("HWUserCleanTaskJobHandler")
    @Transactional(rollbackFor = Exception.class)
    @MDCLog(description = "定时任务：清理华为用户")
    public void jobHandler() throws Exception {
        MeetingClient mgrMeetingClient = hwMeetingCommonService.getMgrMeetingClient();
        SearchUsersRequest request = new SearchUsersRequest();
        request.withOffset(1);
        request.withAdminType(SearchUsersRequest.AdminTypeEnum.NUMBER_2);
        request.withLimit(10);
        SearchUsersResponse searchUsersResponse = mgrMeetingClient.searchUsers(request);

        Integer count = searchUsersResponse.getCount();
        Integer maxHwUserCount = meetingConfig.getMaxHwUserCount();

        String maxHwUserThresholdPe = meetingConfig.getMaxHwUserThresholdPe();
        BigDecimal bigDecimal1 = new BigDecimal(count);
        BigDecimal bigDecimal2 = new BigDecimal(maxHwUserCount);
        BigDecimal bigDecimal3 = new BigDecimal(maxHwUserThresholdPe);
        //判断当前用户数是否已达到华为最大用户阈值
        if (bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_UP).compareTo(bigDecimal3) >= 0) {
            //已超出
            //删除华为用户直到最小华为用户阈值
            //1、不删除当前正在开会、已入会的人
            //2、不删除分配专属会议资源的人
            //3、删除

        }



    }

    public static void main(String[] args) {
        BigDecimal bigDecimal1 = new BigDecimal(8200);
        BigDecimal bigDecimal2 = new BigDecimal(10000);
        BigDecimal bigDecimal3 = new BigDecimal("0.82333333");

        BigDecimal divide = bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_UP);

        System.out.println(divide.compareTo(bigDecimal3));

    }
}
