package com.tiens.meeting.mgr.controller;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@Tag(name = "心跳检测")
/**
 * 心跳检测
 */
public class CheckController {
    /**
     * 心跳ping
     * @return
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
