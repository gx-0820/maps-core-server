package com.example.coreserver.entity.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_radar_data")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private LocalDateTime timestamp;

    
    // We can't directly store a List of RadarTarget objects, so we'll store a JSON representation
    @Column(columnDefinition = "TEXT")
    private String targetsJson;

    // Add a type field for when we're viewing JSON data
    @Column(name = "data_type")
    @Builder.Default
    private String type = "RADAR";
    
    // Add original data in JSON format for reference
    @Column(columnDefinition = "TEXT")
    private String originalJson;
} 
