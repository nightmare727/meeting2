package com.tiens.meeting.web.filter;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static common.exception.enums.GlobalErrorCodeConstants.VERIFICATION;

/**
 * @Description: 过滤器
 * @Author: hh
 * @version:1.0
 */
@Slf4j
public class AuthFilter implements Filter {

    String secretKey= SpringUtil.getProperty("auth.secretKey");

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
        String signature = wrapperRequest.getHeader("authorization");
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

        byte[] data = "我是一段测试字符串".getBytes();
        ImmutableMap<String, String> build =
            ImmutableMap.<String, String>builder().put("nonce", "123456").put("timestamp", "110")
                .put("uri", "/vmeeting/user/11").put("data", "json").build();

        String sign = SecureUtil.signParams(DigestAlgorithm.SHA256, build, "1111111111111111");
        System.out.println("签名：" + sign);

        Sign sign1 = SecureUtil.sign(SignAlgorithm.SHA256withRSA_PSS);
        //验证签名
        System.out.println(sign1.verify(sign.getBytes(), "1111111111111111".getBytes()));
    }
}
