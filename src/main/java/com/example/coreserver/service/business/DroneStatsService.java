package com.example.coreserver.service.business;

import com.example.coreserver.dto.DailyDroneStats;
import com.example.coreserver.entity.DailyDroneDataEntity;
import com.example.coreserver.entity.MonthlyDroneDataEntity;
import com.example.coreserver.repository.DailyDroneDataRepository;
import com.example.coreserver.repository.MonthlyDroneDataRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class DroneStatsService {

    private final DailyDroneDataRepository dailyDroneDataRepository;
    private final MonthlyDroneDataRepository monthlyDroneDataRepository;

    @Getter
    private volatile DailyDroneStats todayStats = new DailyDroneStats();

    private String currentYearMonth;

    @Autowired
    public DroneStatsService(DailyDroneDataRepository dailyDroneDataRepository, MonthlyDroneDataRepository monthlyDroneDataRepository) {
        this.dailyDroneDataRepository = dailyDroneDataRepository;
        this.monthlyDroneDataRepository = monthlyDroneDataRepository;
        updateCurrentYearMonth();
    }

    private void updateCurrentYearMonth() {
        currentYearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public void updateDailyStats() {
        LocalDate nowDate = LocalDate.now();

        // 检查数据库中是否已存在当天的记录
        Optional<DailyDroneDataEntity> existingEntity = dailyDroneDataRepository.findByDate(nowDate);

        if (existingEntity.isPresent()) {
            // 如果存在，更新现有记录
            DailyDroneDataEntity entityToUpdate = existingEntity.get();
            entityToUpdate.setDroneCount(todayStats.getDroneCount());
            entityToUpdate.setIllegalDroneCount(todayStats.getIllegalDroneCount());
            entityToUpdate.setDisposedDroneCount(todayStats.getDisposedDroneCount());
            entityToUpdate.setUpdatedAt(LocalDateTime.now());
            dailyDroneDataRepository.save(entityToUpdate);
        } else {
            // 如果不存在，创建新记录
            DailyDroneDataEntity dailyEntity = new DailyDroneDataEntity();
            dailyEntity.setDroneCount(todayStats.getDroneCount());
            dailyEntity.setIllegalDroneCount(todayStats.getIllegalDroneCount());
            dailyEntity.setDisposedDroneCount(todayStats.getDisposedDroneCount());
            dailyEntity.setDate(nowDate);
            dailyEntity.setCreatedAt(LocalDateTime.now());
            dailyEntity.setUpdatedAt(LocalDateTime.now());
            dailyDroneDataRepository.save(dailyEntity);
        }

    }

    private void updateMonthlyStats() {
        // 检查是否存在当月的月统计数据
        Optional<MonthlyDroneDataEntity> monthlyEntityOpt = monthlyDroneDataRepository.findByYearMonth(currentYearMonth);

        MonthlyDroneDataEntity monthlyEntity;

        if (monthlyEntityOpt.isPresent()) {
            // 如果存在，更新现有记录
            monthlyEntity = monthlyEntityOpt.get();
            monthlyEntity.setDroneCount(monthlyEntity.getDroneCount() + todayStats.getDroneCount());
            monthlyEntity.setIllegalDroneCount(monthlyEntity.getIllegalDroneCount() + todayStats.getIllegalDroneCount());
            monthlyEntity.setDisposedDroneCount(monthlyEntity.getDisposedDroneCount() + todayStats.getDisposedDroneCount());
            monthlyEntity.setUpdatedAt(LocalDateTime.now());
        } else {
            // 如果不存在，创建新记录
            monthlyEntity = new MonthlyDroneDataEntity();
            monthlyEntity.setYearMonth(currentYearMonth);
            monthlyEntity.setDroneCount(todayStats.getDroneCount());
            monthlyEntity.setIllegalDroneCount(todayStats.getIllegalDroneCount());
            monthlyEntity.setDisposedDroneCount(todayStats.getDisposedDroneCount());
            monthlyEntity.setTimestamp(LocalDateTime.now());
            monthlyEntity.setCreatedAt(LocalDateTime.now());
            monthlyEntity.setUpdatedAt(LocalDateTime.now());
        }

        // 保存月统计数据
        monthlyDroneDataRepository.save(monthlyEntity);
    }

    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜执行
    public void resetDailyStats() {
        // 重置今日统计数据
        todayStats = new DailyDroneStats();
        updateCurrentYearMonth();
        updateMonthlyStats();
    }

    public void droneCount() {
        todayStats.setDroneCount(todayStats.getDroneCount() + 1);
        updateDailyStats();
    }

    public void incrementDisposedDroneCount() {
        todayStats.setDisposedDroneCount(todayStats.getDisposedDroneCount() + 1);
        updateDailyStats();
    }

    public void incrementIllegalDroneCount() {
        todayStats.setIllegalDroneCount(todayStats.getIllegalDroneCount() + 1);
        updateDailyStats();
    }
}