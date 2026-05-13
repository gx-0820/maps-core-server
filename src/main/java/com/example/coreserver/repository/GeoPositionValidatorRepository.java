package com.example.coreserver.repository;

import com.example.coreserver.entity.algorithm.db.GeoPositionValidatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeoPositionValidatorRepository extends JpaRepository<GeoPositionValidatorEntity, Long> {
    GeoPositionValidatorEntity findTopByOrderByCreatedAtDesc();
} 