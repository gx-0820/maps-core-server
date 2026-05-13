package com.example.coreserver.entity.algorithm.db;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "algorithm_data_fusion")
public class DataFusionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "target_id")
    private Integer targetId;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "altitude")
    private Double altitude;
    
    @Column(name = "velocity")
    private Double velocity;
    
    @Column(name = "azimuth")
    private Double azimuth;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "distance")
    private Double distance;
    
    @Column(name = "pitch")
    private Double pitch;
    
    @Column(name = "threat_level")
    private String threatLevel;
    
    @Column(name = "pan_angle")
    private Double panAngle;
    
    @Column(name = "tilt_angle")
    private Double tiltAngle;
    
    @Column(name = "zoom_level")
    private Double zoomLevel;
    
    @Column(name = "color")
    private String color;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone="GMT+8")
    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 