package com.example.coreserver.repository.algorithm;

import com.example.coreserver.entity.algorithm.db.DataFusionTargetEntity;
import com.example.coreserver.entity.algorithm.db.DataFusionTargetEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author codex
 * @description 融合目标仓库。
 */
@Repository
public interface DataFusionTargetRepository extends JpaRepository<DataFusionTargetEntity, DataFusionTargetEntityId> {
    List<DataFusionTargetEntity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
}
