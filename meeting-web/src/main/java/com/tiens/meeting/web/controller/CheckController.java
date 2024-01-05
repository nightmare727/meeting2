package com.tiens.meeting.web.controller;

import com.tiens.api.service.TestDubboService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.Reference;
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
        testDubboService.hello("wenjie");
    }
}
