package com.tiens.meeting.web.controller;

import com.tiens.api.service.TestDubboService;
import com.tiens.meeting.web.filter.CustomDubboFilter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName CheckController
 * @Author 赵海山
 * @Date 2021/3/13
 * @Discription
 */

@RestController
@Tag(name = "心跳检测")
public class CheckController {

    @Reference(filter = "logFilter")
    TestDubboService testDubboService;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/")
    public String check() {
        return "pong";
    }

    @GetMapping("/testDubbo")
    public void testDubbo() {
        RpcContext.getContext().setAttachment("t1","s1");
        RpcContext.getContext().setAttachment("t2","s2");
        RpcContext.getContext().setAttachment("t3","s2");
        testDubboService.hello("wenjie");
    }

    @PostMapping("/testAuth")
    public CommonResult testAuth(@RequestBody MeetingResourceAwardDTO meetingResourceAwardDTO) {
        return CommonResult.success("校验成功");
    }
}
