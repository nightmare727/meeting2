package com.tiens.meeting.dubboservice.mock;

import com.tiens.china.circle.api.bo.ComplaintsBo;
import com.tiens.china.circle.api.bo.HomepageBo;
import com.tiens.china.circle.api.common.page.Pager;
import com.tiens.china.circle.api.common.result.PagerResult;
import com.tiens.china.circle.api.common.result.Result;
import com.tiens.china.circle.api.dto.HomepageUserDTO;
import com.tiens.china.circle.api.dto.UserInfoDTO;
import com.tiens.china.circle.api.dto.UserPrivacySwitchDTO;
import com.tiens.china.circle.api.dto.UserUnreadNumDTO;
import com.tiens.china.circle.api.vo.CommunityReviewRecordVo;
import org.springframework.stereotype.Component;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/13
 * @Version 1.0
 * @Company: tiens
 */
@Component
public class DubboCommonUserServiceMock {
    public Result<?> selectAllPush(HomepageBo homepageBo) {
        return null;
    }

    public Integer queryOwnSelectAllPushByAccId(String accid) {
        return null;
    }

    public Result<?> authPush(HomepageBo homepageBo) {
        return null;
    }

    public Integer queryOwnPublishStateByAccId(String accid) {
        return null;
    }

    public Result<UserUnreadNumDTO> queryOwnNumtotalByAccId(String accid) {
        return null;
    }

    public Result<UserInfoDTO> queryOwnInfoByAccId(String accid, String joyoCode) {
        return null;
    }

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

    public PagerResult<CommunityReviewRecordVo> commentRecord(String userId, Pager pager) {
        return null;
    }

    public Result updateNotification(Integer type, String userId) {
        return null;
    }

    public void deleteUserInfoRedis(String accid, String publisherid) {

    }

    public void deleteNewNumFormRedis(String accid) {

    }

    public Result<?> saveComplaintsInfo(ComplaintsBo complaintsBo) {
        return null;
    }

    public void sendLogInMq(String accid) {

    }

    public Result<?> queryUserVersion() {
        return null;
    }

    public Result<?> queryUserVersionOverseas() {
        return null;
    }

    public Result<?> queryUserPrivacySwitch(String accId) {
        return null;
    }

    public Result<?> setUserPrivacySwitch(UserPrivacySwitchDTO privacySwitchDTO) {
        return null;
    }
}
