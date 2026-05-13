package com.example.coreserver.repository;

import com.example.coreserver.entity.MonthlyDroneDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyDroneDataRepository extends JpaRepository<MonthlyDroneDataEntity, Long> {

    /**
     * 根据年月查找记录
     */
    Optional<MonthlyDroneDataEntity> findByYearMonth(String yearMonth);


    /**
     * 根据时间范围查找记录
     */
    List<MonthlyDroneDataEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 获取最近的记录
     */
    List<MonthlyDroneDataEntity> findTop12ByOrderByYearMonthDesc();

    /**
     * 按年月范围查询
     */
    @Query("SELECT m FROM MonthlyDroneDataEntity m WHERE m.yearMonth BETWEEN :startMonth AND :endMonth ORDER BY m.yearMonth")
    List<MonthlyDroneDataEntity> findByYearMonthBetween(@Param("startMonth") String startMonth, @Param("endMonth") String endMonth);

    /**
     * 统计总无人机数量
     */
    @Query("SELECT SUM(m.droneCount) FROM MonthlyDroneDataEntity m WHERE m.yearMonth = :yearMonth")
    Long sumDroneCountByYearMonth(@Param("yearMonth") String yearMonth);
}
