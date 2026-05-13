package com.example.coreserver.repository;

import com.example.coreserver.entity.TargetMonitorStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetMonitorStatRepository extends JpaRepository<TargetMonitorStatEntity, Long> {

    Optional<TargetMonitorStatEntity> findByStatTime(LocalDate statTime);

    List<TargetMonitorStatEntity> findByStatTimeBetweenOrderByStatTimeAsc(LocalDate startDate, LocalDate endDate);
}
