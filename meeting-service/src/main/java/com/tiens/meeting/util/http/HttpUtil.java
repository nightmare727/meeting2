package com.tiens.meeting.util.http;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName HttpUtil
 * @Description T0D0
 * Author HP
 * @Date 2023/3/3 9:58
 * Version 1.0
 **/
@Slf4j
public class HttpUtil {

    public static String requestByGet(String url, Map<String, Object> map, HttpHeaders headers) {
        RestTemplate restTemplate = new RestTemplate();
        // header填充
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        ResponseEntity<String> responseEntity;
        //如果存在參數
        if (!map.isEmpty()) {
            for (Map.Entry<String, Object> e :
                    map.entrySet()) {
                //构建查询参数
                builder.queryParam(e.getKey(), e.getValue());
            }
            //拼接好参数后的URl//test.com/url?param1={param1}&param2={param2};
            String reallyUrl = builder.build().toString();
            try {
                responseEntity = restTemplate.exchange(reallyUrl, HttpMethod.GET, request, String.class);
            } catch (Exception ex) {
                System.out.println("访问出错了,错误原因:" + ex.getMessage());
                return ex.getMessage();
            }
        } else {
            try {
                responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            } catch (Exception ex) {
                return ex.getMessage();
            }
        }
        return responseEntity.getBody();
    }

    public static String requestByPost(String url, Map<String, Object> map, HttpHeaders headers){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(15000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(url, request, String.class);
        } catch (Exception ex) {
            log.info("HttpTool---------requestByPost请求告警:{}",ex.getMessage());
            return ex.getMessage();
        }
        return responseEntity.getBody();
    }

    public static void main(String[] args) {
        /*云购*/

        HashMap<String, Object> map = new HashMap<>();
        map.put("joyo_code","66838286");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("appKey","091d400bd8ee7f27");
        httpHeaders.add("app_version","5.1.3");
        String s = requestByGet("https://dev-mshop3.tiens.com.cn/unicom/public/user/userifexist", map, httpHeaders);
        JSONObject jsonObject = JSONObject.parseObject(s);
        Object code = jsonObject.get("code");
        System.out.println(code);

        /*vshare*/
        /*HashMap<String, Object> map = new HashMap<>();
        map.put("distributor_id","89153554");
        HttpHeaders headers = new HttpHeaders();
        String s = requestByGet("https://dev2-ir-kong.tiens.com/user-aggr/public/user/userifexist", map, null);
        System.out.println(s);*/
    }
}
