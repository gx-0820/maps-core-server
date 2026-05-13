package com.example.coreserver.repository;

import com.example.coreserver.entity.DailyDroneDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyDroneDataRepository extends JpaRepository<DailyDroneDataEntity, Long> {
    // 定义按日期查询的方法
    Optional<DailyDroneDataEntity> findByDate(LocalDate date);
}