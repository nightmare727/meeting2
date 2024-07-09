package com.tiens.meeting.dubboservice.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/9
 * @Version 1.0
 * @Company: tiens
 */
@Service(version = "1.0")
@RequiredArgsConstructor
@Slf4j
public class MeetingCacheServiceImpl implements Serializable {

}
