package com.tiens.meeting.dubboservice.async;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.meeting.dubboservice.bo.UpdateVMAnchorInfoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Author: 蔚文杰
 * @Date: 2024/4/11
 * @Version 1.0
 * @Company: tiens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAsyncTaskServiceImpl implements UserAsyncTaskService {

    @Autowired
    RedissonClient redissonClient;
    /**
     * 海外主播用户修改
     */
    @Value("${live.commerceUserModUrl}")
    String commerceUserModUrl;
    /**
     * 国内主播用户同步
     */
    @Value("${live.domesticUserModUrl}")
    String domesticUserModUrl;

    @Override
    public void updateLiveAnchorInfo(HomepageUserDTO homepageUserDTO) {
        String nickName = homepageUserDTO.getNickName();
        String headImg = homepageUserDTO.getHeadImg();

        String country = homepageUserDTO.getCountry();
        String accid = homepageUserDTO.getAccid();
        String joyoCode = homepageUserDTO.getJoyo_code();

        //查询缓存
       /* RBucket<VMUserVO> bucket = redissonClient.getBucket(CacheKeyUtil.getUserInfoKey(accid));

        VMUserVO vmUserCacheVO = bucket.get();
        //数据为空，未知用户则直接同步
        if (ObjectUtil.isNotNull(vmUserCacheVO)) {
            //头像没，昵称没变化
            boolean equals = vmUserCacheVO.getHeadImg().equals(headImg) && vmUserCacheVO.getNickName().equals(nickName);
            if (equals) {
                log.info("VM用户头像昵称均无变化，无需操作");
                return;
            }
        }*/
        UpdateVMAnchorInfoRequest updateVMAnchorInfoRequest = new UpdateVMAnchorInfoRequest();
        updateVMAnchorInfoRequest.setHeadUrl(headImg);
        updateVMAnchorInfoRequest.setAnchorName(nickName);
        updateVMAnchorInfoRequest.setJoyoCode(joyoCode);

        //同步直播主播修改
        try {
            String result = HttpUtil.post("CN".equals(country) ? domesticUserModUrl : commerceUserModUrl,
                JSON.toJSONString(updateVMAnchorInfoRequest), 10000);
            log.info("主播用户数据同步入参：{},结果：{}", JSON.toJSONString(updateVMAnchorInfoRequest), result);
        } catch (Exception e) {
            log.error("主播用户数据同步异常，错误信息：{}", e);
        }

    }

}
