package com.tiens.api.service;

import com.tiens.api.vo.VMMeetingCredentialVO;
import common.pojo.CommonResult;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/9
 * @Version 1.0
 * @Company: tiens
 */
public interface RpcMeetingRoomService {
    /**
     * 获取认证资质
     *
     * @param userId
     * @return
     */
    CommonResult<VMMeetingCredentialVO> getCredential(String userId);

}
