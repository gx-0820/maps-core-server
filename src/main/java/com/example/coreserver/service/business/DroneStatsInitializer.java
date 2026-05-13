package com.example.coreserver.service.business;

import com.example.coreserver.repository.DailyDroneDataRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DroneStatsInitializer implements InitializingBean {

    @Autowired
    private DailyDroneDataRepository dailyDroneDataRepository;

    @Autowired
    private DroneStatsService droneStatsService;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 尝试初始化每日无人机统计数据
            droneStatsService.resetDailyStats();
        } catch (Exception e) {
            // 捕获并记录初始化过程中的异常
            // 根据业务需求决定是否重新抛出异常
            System.err.println("Failed to initialize drone stats: " + e.getMessage());
            e.printStackTrace();
            // 如果需要在初始化失败时停止应用，可以抛出异常
            // throw e;
        }
    }
}