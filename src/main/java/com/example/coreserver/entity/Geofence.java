package com.example.coreserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "geofence")
public class Geofence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 圆心经度
    @Column(name = "core_longitude", nullable = false, precision = 10)
    private Double coreLongitude;

    // 圆心纬度
    @Column(name = "core_latitude", nullable = false, precision = 10)
    private Double coreLatitude;

    // 核心半径
    @Column(name = "core_radius", nullable = false, precision = 12)
    private Double coreRadius;

    // 缓冲半径
    @Column(name = "buffer_radius", nullable = false, precision = 12)
    private Double bufferRadius;

    // 报警半径
    @Column(name = "alert_radius", nullable = false, precision = 12)
    private Double alertRadius;

//    public Double getcoreRadius(){
//        return coreRadius;
//    }
//
//    public Double getcorebufferRadius(){
//        return bufferRadius;
//    }
//
//    public Double getalertRadius(){
//        return alertRadius;
//    }
}