package com.example.coreserver.repository;

import com.example.coreserver.entity.algorithm.db.TrackPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackPredictionRepository extends JpaRepository<TrackPredictionEntity, Long> {
    List<TrackPredictionEntity> findByTargetIdOrderBySequenceNumber(Long targetId);
} 