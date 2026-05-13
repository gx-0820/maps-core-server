package com.example.coreserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "target_monitor_stat")
public class TargetMonitorStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_time", nullable = false)
    private LocalDate statTime;

    @Column(name = "radar_target_count", nullable = false)
    private Integer radarTargetCount;

    @Column(name = "tdoa_target_count", nullable = false)
    private Integer tdoaTargetCount;

    @Column(name = "fusion_target_count", nullable = false)
    private Integer fusionTargetCount;

    @Column(name = "radar_illegal_count", nullable = false)
    private Integer radarIllegalCount;

    @Column(name = "tdoa_illegal_count", nullable = false)
    private Integer tdoaIllegalCount;

    @Column(name = "fusion_illegal_count", nullable = false)
    private Integer fusionIllegalCount;

    @Column(name = "need_dispose_count", nullable = false)
    private Integer needDisposeCount;

    @Column(name = "effective_dispose_count", nullable = false)
    private Integer effectiveDisposeCount;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
