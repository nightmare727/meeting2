package com.tiens.meeting.web.filter;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static common.exception.enums.GlobalErrorCodeConstants.VERIFICATION;

/**
 * @Description: 过滤器
 * @Author: hh
 * @version:1.0
 */
@Slf4j
public class AuthFilter implements Filter {

    String secretKey = SpringUtil.getProperty("auth.secretKey");

    public static final String signPrefix = "HMAC-SHA256 signature=";

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HeaderMapRequestWrapper wrapperRequest = new HeaderMapRequestWrapper(httpServletRequest);

        if (!verify(wrapperRequest)) {
            //验证失败，返回异常
            response.getWriter().write(JSON.toJSONString(CommonResult.error(VERIFICATION)));
            response.flushBuffer();
            return;
        }
        chain.doFilter(wrapperRequest, response);
    }

    boolean verify(HeaderMapRequestWrapper wrapperRequest) throws IOException {
        final String requestURI = wrapperRequest.getRequestURI();
        String nonce = wrapperRequest.getHeader("nonce");
        String timeStamp = wrapperRequest.getHeader("timestamp");

        String md5Str = nonce + "&" + timeStamp + "&" + secretKey;
        String s = MD5.create().digestHex(md5Str);
        String signature = wrapperRequest.getHeader("authorization");

        if (s.equals(signature)) {
            return true;
        }
        String json = getJson(wrapperRequest);
        String requestJson = StrUtil.removeAllLineBreaks(json);

        ImmutableMap<String, String> build =
            ImmutableMap.<String, String>builder().put("nonce", nonce).put("timestamp", timeStamp)
                .put("uri", requestURI).put("data", requestJson).build();
        String sign = SecureUtil.signParams(DigestAlgorithm.SHA256, build, "&", "=", true, secretKey);
        String requestSignature = StrUtil.removePrefix(signature, signPrefix);
        return requestSignature.equals(sign);

    }

    public String getJson(HttpServletRequest request) throws IOException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            sb.append(StrUtil.replace(inputStr, " ", ""));
        }
        return sb.toString();
    }

    public static void main(String[] args) {

        String s = RandomUtil.randomString(16);

        String md5Str = "1234567890" + "&" + "1720057260" + "&" + "uz06bl49uhy7kwxq";

        System.out.println(MD5.create().digestHex(md5Str));

        String data = "{\"joyoCode\":\"67893223\",\"nationId\":\"CN\",\"orderNo\":\"20240704093849492856\"," +
            "\"skuId\":\"11021765\",\"accId\":1123,\"orderStatus\":\"4\",\"paidVmAmount\":\"5\",\"paidRealAmount\":0}";

        ImmutableMap<String, String> build =
            ImmutableMap.<String, String>builder().put("nonce", "1234567890").put("timestamp", "1720057260")
                .put("uri", "/vmeeting/web/profit/pushOrder").put("data", data).build();

        String sign = SecureUtil.signParams(DigestAlgorithm.SHA256, build, "&", "=", true, "uz06bl49uhy7kwxq");
        System.out.println(sign);
        System.out.println(
            HexUtil.encodeHexStr("1fe5b3fda3547c95c3589fa418b67ce779482bc5ab231ac64da38d439518db68".getBytes(
                StandardCharsets.UTF_8)));
        //验证签名
//        System.out.println(sign1.verify(sign.getBytes(), "1111111111111111".getBytes()));
    }
}
