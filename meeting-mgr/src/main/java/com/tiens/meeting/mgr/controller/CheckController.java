package com.tiens.meeting.mgr.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
