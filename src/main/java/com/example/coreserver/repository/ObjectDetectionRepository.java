package com.example.coreserver.repository;

import com.example.coreserver.entity.algorithm.db.ObjectDetectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectDetectionRepository extends JpaRepository<ObjectDetectionEntity, Long> {
} 