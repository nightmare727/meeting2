package com.tiens.meeting.util;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class WheelTimerContext {

    private volatile static WheelTimerContext instance;

    public static WheelTimerContext getInstance() {
        if (null == instance) {
            synchronized (WheelTimerContext.class) {
                if (null == instance) {
                    instance = new WheelTimerContext();
                }
            }
        }
        return instance;
    }

    private final Timer wheelTimer;

    private WheelTimerContext() {
        wheelTimer = new HashedWheelTimer(500, TimeUnit.MILLISECONDS, 32);
    }

    public void createTimeoutTask(TimerTask task, long delay, TimeUnit unit) {
        wheelTimer.newTimeout(task, delay, unit);
    }

    public static void main(String[] args) {
        Timer timer = new HashedWheelTimer(500, TimeUnit.MILLISECONDS, 32);
        ThreadFactory threadFactory =
            ThreadFactoryBuilder.create().setNamePrefix("WheelTimerContext-").setUncaughtExceptionHandler((t, e) -> {

            }).setThreadFactory(r -> new Thread(r)).build();

        Timeout timeout1 = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                System.out.println("timeout1: " + new Date());
            }
        }, 10, TimeUnit.SECONDS);
        if (!timeout1.isExpired()) {
            timeout1.cancel();
        }
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws InterruptedException {
                System.out.println("timeout2: " + new Date());
            }
        }, 1, TimeUnit.SECONDS);
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                System.out.println("timeout3: " + new Date());
            }
        }, 3, TimeUnit.SECONDS);

    }
}

