package com.tiens.meeting.util;

import com.alibaba.fastjson.JSONObject;
import com.tiens.meeting.util.http.HttpUtil;
import common.pojo.CommonResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

/**
 * @Author: 谷守丙
 * @Date: 2023/10/20
 * @Version 1.0
 * 根据经销商号/卓越卡号，和国内外标识来判断当前用户的身份等级
 */
@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class GetIdentityUtil {

    @Value("${vshare.url}")
    private  String vshareUrl;

    @Value("${vshare.path}")
    private  String vsharePath;

    public  CommonResult getIdentity(String joyoCode,Integer regionType){
        //根据经销商编号/卓越卡号来获取用户的实时身份等级
        HashMap<String, Object> map = new HashMap<>();
        map.put("disid",joyoCode);
        map.put("type",regionType);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("tiens_token","appmustsucceed");
        httpHeaders.add("distributor_id",joyoCode);
        log.info("调用vshare接口参数及请求头, 参数：{};请求头：{}",map, JSONObject.toJSONString(httpHeaders));
        String request = HttpUtil.requestByGet(vshareUrl+vsharePath, map, httpHeaders);
        JSONObject jsonObject = JSONObject.parseObject(request);
        log.info("调用vshare接口返回值:{}",jsonObject);
        String jsonObjectcode = jsonObject.getString("code");
        if(StringUtils.isEmpty(jsonObjectcode)){
            return CommonResult.success(null);
        }
        if (jsonObjectcode.equals("50000")){
            return CommonResult.success(null);
        }
        String jsonObjectString = jsonObject.getString("data");
        HashMap map1=JSONObject.parseObject(jsonObjectString, HashMap.class);
        Integer checkMapLevel = (Integer) Optional.ofNullable(map1.get("xj")).orElse(null);
        return CommonResult.success(checkMapLevel);
    }
}
