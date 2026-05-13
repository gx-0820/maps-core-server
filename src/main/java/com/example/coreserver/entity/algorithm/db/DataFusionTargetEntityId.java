package com.example.coreserver.entity.algorithm.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 融合目标表复合主键。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataFusionTargetEntityId implements Serializable {
    private String id;
    private LocalDateTime timestamp;
}
