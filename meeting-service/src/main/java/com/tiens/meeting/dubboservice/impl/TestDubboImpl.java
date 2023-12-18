package com.tiens.meeting.dubboservice.impl;

import com.tiens.api.service.TestDubboService;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;

import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0", filter = "testProviderFilter")
public class TestDubboImpl implements TestDubboService {
    @Override
    public String hello(String param) {
        RpcContext context = RpcContext.getContext();
        Map<String, String> attachments = context.getAttachments();
        System.out.println(attachments);
        return param + ", hello";
    }
}
