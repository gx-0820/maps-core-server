package com.example.coreserver.repository;

import com.example.coreserver.entity.algorithm.db.DataFusionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataFusionRepository extends JpaRepository<DataFusionEntity, Long> {
    DataFusionEntity findTopByOrderByCreatedAtDesc();
    List<DataFusionEntity> findTop100ByOrderByCreatedAtDesc();
    List<DataFusionEntity> findByTargetId(Integer targetId);
} 