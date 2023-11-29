package com.tiens.meeting.dubboservice.mock;

import cn.hutool.core.util.RandomUtil;
import com.tiens.china.circle.api.bo.ComplaintsBo;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.page.Pager;
import com.tiens.china.circle.api.common.result.PagerResult;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.china.circle.api.dto.UserInfoDTO;
import com.tiens.china.circle.api.dto.UserUnreadNumDTO;
import com.tiens.china.circle.api.dubbo.DubboCommonUserService;
import com.tiens.china.circle.api.vo.CommunityReviewRecordVo;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@Component
public class DubboCommonUserServiceMock implements DubboCommonUserService {
    @Override
    public Result<?> selectAllPush(HomepageBo homepageBo) {
        return null;
    }

    @Override
    public Integer queryOwnSelectAllPushByAccId(String accid) {
        return null;
    }

    @Override
    public Result<?> authPush(HomepageBo homepageBo) {
        return null;
    }

    @Override
    public Integer queryOwnPublishStateByAccId(String accid) {
        return null;
    }

    @Override
    public Result<UserUnreadNumDTO> queryOwnNumtotalByAccId(String accid) {
        return null;
    }

    @Override
    public Result<UserInfoDTO> queryOwnInfoByAccId(String accid, String joyoCode) {
        return null;
    }

    @Override
    public Result<HomepageUserDTO> queryUserInfoAccId(String ownAccId, HomepageBo homepageBo) {
        HomepageUserDTO homepageUserDTO = new HomepageUserDTO();
        homepageUserDTO.setAccid("h5v4qv8wl6916xld599q2vwkyrnncb9lfkj7kmh1");
        homepageUserDTO.setJoyo_code("123");
        homepageUserDTO.setMobile("18210515311");
        homepageUserDTO.setEmail("134011111111@163.com");
        homepageUserDTO.setNickName("jack");
        homepageUserDTO.setHeadImg("");
        homepageUserDTO.setState("1");
        homepageUserDTO.setSource("1");
        homepageUserDTO.setFollowersNum("");
        homepageUserDTO.setFansNum("");
        homepageUserDTO.setSex("");
        homepageUserDTO.setPersonalProfile("");
        homepageUserDTO.setLevelCode(0);
        homepageUserDTO.setGiftNumbers(0);
        homepageUserDTO.setCountry("");

        return Result.success(homepageUserDTO);
    }

    @Override
    public PagerResult<CommunityReviewRecordVo> commentRecord(String userId, Pager pager) {
        return null;
    }

    @Override
    public Result updateNotification(Integer type, String userId) {
        return null;
    }

    @Override
    public void deleteUserInfoRedis(String accid, String publisherid) {

    }

    @Override
    public void deleteNewNumFormRedis(String accid) {

    }

    @Override
    public Result<?> saveComplaintsInfo(ComplaintsBo complaintsBo) {
        return null;
    }

    @Override
    public void sendLogInMq(String accid) {

    }

    @Override
    public Result<?> queryUserVersion() {
        return null;
    }

    @Override
    public Result<?> queryUserVersionOverseas() {
        return null;
    }
}
