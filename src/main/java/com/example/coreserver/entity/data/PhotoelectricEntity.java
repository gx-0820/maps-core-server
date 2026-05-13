package com.example.coreserver.entity.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_photoelectric_data")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoelectricEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private LocalDateTime timestamp;
    // Add a type field for when we're viewing JSON data
    @Column(name = "data_type")
    private String type;  // PE_UDP or PE_RTSP based on the input type
    
    // Add original data in JSON format for reference
    @Column(columnDefinition = "TEXT")
    private String originalJson;
} 