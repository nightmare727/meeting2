package com.tiens.meeting.web.controller;

import com.tiens.meeting.web.entity.resp.CredentialResponse;
import com.tiens.meeting.web.util.HwClientUtil;
import common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author generator
 * @version 1.0
 * @Title: AuthController
 * @Description: 文件分类表
 */

@RestController
@RequestMapping("auth")
@Tag(name = "会议权限接口")
public class AuthController {

    @Value("${hw.obs.endPoint}")//obs.cn-north-4.myhuaweicloud.com
    private String endPoint;

    @Value("${hw.obs.bucket:china-middle-test}")
    private String bucket;

    @Value("${hw.obs.accessDomain}")
    private String accessDomain;

    @Value("${hw.obs.acceleratedDomain}")
    private String acceleratedDomain;

    @Operation(summary = "获取华为云临时token")
    @RequestMapping(value = "/tempToken", method = RequestMethod.GET)
    public CommonResult<CredentialResponse> tempToken() {
        CredentialResponse temporaryAccessKeyByToken = HwClientUtil.getTemporaryAccessKeyByToken();
        temporaryAccessKeyByToken.setBucket(bucket);
        temporaryAccessKeyByToken.setEndPoint(endPoint);
        temporaryAccessKeyByToken.setAccessDomain(accessDomain);
        temporaryAccessKeyByToken.setAcceleratedDomain(acceleratedDomain);
        return CommonResult.success(temporaryAccessKeyByToken);
    }

}
