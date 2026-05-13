package com.example.coreserver.repository.data;

import com.example.coreserver.entity.data.ElectricInvestigationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ElectricInvestigationEntityRepository extends JpaRepository<ElectricInvestigationEntity, Long> {
    List<ElectricInvestigationEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<ElectricInvestigationEntity> findTop100ByOrderByTimestampDesc();
}