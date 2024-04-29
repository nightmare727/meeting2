package com.tiens.meeting.dubboservice.common;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.http.param.MediaType;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.tiens.meeting.dubboservice.common.entity.NewComerTasksModel;
import com.tiens.meeting.dubboservice.common.entity.SyncCommonResult;
import com.tiens.meeting.dubboservice.config.MeetingConfig;
import com.tiens.meeting.util.VmUserUtil;
import common.util.cache.CacheKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author: songxh@tiens.com
 * @version: 0.1.0
 * @className: WebLogAspect
 * @description: 日志切面织入
 * @create: 2021/4/9
 **/
@Slf4j
@Aspect
@Component
public class CommonAspect {

    //    @Value("${rocketmq.producer.push_new_comer_task_topic}")
    String pushNewComerTasksUrl;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    MeetingConfig meetingConfig;

    ListeningExecutorService listeningExecutorService =
        MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    @Pointcut("@annotation(com.tiens.meeting.dubboservice.common.NewComerTasks)")
    public void newComerTasks() {
    }

    /**
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @AfterReturning("newComerTasks()")
    public void doNewComerTasksAfterReturning(JoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        Object arg = args[0];

        String getImUserId = (String)arg.getClass().getMethod("getImUserId").invoke(arg);
        RBucket<String> bucket = redissonClient.getBucket(CacheKeyUtil.getNewComerTaskUserKey(getImUserId));

        if (bucket.isExists()) {
            return;
        }
        NewComerTasksModel newComerTasksModel = new NewComerTasksModel();
        newComerTasksModel.setCountry("cn");
        newComerTasksModel.setSource(1);
        newComerTasksModel.setCoinSource(73);

        Map<String, String> authHead = VmUserUtil.getAuthHead(getImUserId);
        String param = JSON.toJSONString(newComerTasksModel);

        listeningExecutorService.submit(() -> {

            HttpResponse execute = HttpUtil.createPost(meetingConfig.getNewComerTasksSynUrl()).addHeaders(authHead)
                .body(param, MediaType.APPLICATION_JSON).execute();
            String result = execute.body();

            SyncCommonResult syncCommonResult = JSON.parseObject(result, SyncCommonResult.class);
            if (syncCommonResult.getSuccess()) {
                //设置新人奖励同步结果
                bucket.set("1");
                log.info("同步会议新人奖励成功请求头：{}，参数：{}，返回结果：{}", authHead, param, result);
            } else {
                log.info("同步会议新人奖励失败，请求头：{}，参数：{}，返回结果：{}", authHead, param, result);

            }
        });

    }

}
