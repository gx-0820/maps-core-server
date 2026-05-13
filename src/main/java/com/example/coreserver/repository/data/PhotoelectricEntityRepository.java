package com.example.coreserver.repository.data;

import com.example.coreserver.entity.data.PhotoelectricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoelectricEntityRepository extends JpaRepository<PhotoelectricEntity, Long> {
    List<PhotoelectricEntity> findByDeviceId(String deviceId);
    List<PhotoelectricEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<PhotoelectricEntity> findByType(String type);
    List<PhotoelectricEntity> findTop100ByOrderByTimestampDesc();
} 