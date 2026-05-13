package com.example.coreserver.entity.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_electric_investigation_data")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectricInvestigationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Add a type field that matches the original data type
    @Column(name = "data_type")
    @Builder.Default
    private String type = "ELECTRIC_UAV";
    
    private LocalDateTime updateTime;
    
    // Add timestamp for consistency with other entities
    private LocalDateTime timestamp;
    
    // Add original data in JSON format for reference
    @Column(columnDefinition = "TEXT")
    private String originalJson;
} 
