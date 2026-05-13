package com.example.coreserver.entity.algorithm.db;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "algorithm_object_detection")
public class ObjectDetectionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "target_left")
    private Integer left;
    
    @Column(name = "target_top")
    private Integer top;
    
    @Column(name = "target_width")
    private Integer width;
    
    @Column(name = "target_height")
    private Integer height;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 