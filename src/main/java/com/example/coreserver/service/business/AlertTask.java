package com.example.coreserver.service.business;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 保留，定时任务可以考虑由前端定时调用接口，不在后端开启定时任务
 */
public class AlertTask {

    // 创建一个调度执行器服务
    static private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static public void run() {
        // 定义一个任务
        Runnable task = () -> {
            System.out.println("定时任务执行: " + System.currentTimeMillis());

        };

        // 安排任务在初始延迟1秒后，每隔100毫秒执行一次
        scheduler.scheduleAtFixedRate(task, 1, 100, TimeUnit.MILLISECONDS);
    }



}
