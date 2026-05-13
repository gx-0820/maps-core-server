package com.example.coreserver.repository.data;

import com.example.coreserver.entity.data.RadarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RadarEntityRepository extends JpaRepository<RadarEntity, Long> {
    List<RadarEntity> findByDeviceId(String deviceId);
    List<RadarEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<RadarEntity> findTop100ByOrderByTimestampDesc();
} 