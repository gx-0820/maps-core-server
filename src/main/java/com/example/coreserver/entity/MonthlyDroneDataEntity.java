package com.example.coreserver.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 每月无人机数据记录实体类
 */
@Data
@Entity
@Table(name = "monthly_drone_data")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyDroneDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "`year_month`", nullable = false, unique = true)
    private String yearMonth;  // 年月，格式：2024-01

    @Column(name = "drone_count", nullable = false)
    @Builder.Default
    private Integer droneCount = 0;  // 无人机数量

    @Column(name = "illegal_drone_count", nullable = false)
    @Builder.Default
    private Integer illegalDroneCount = 0;  // 非法无人机数量

    @Column(name = "disposed_drone_count", nullable = false)
    @Builder.Default
    private Integer disposedDroneCount = 0;  // 处置无人机数量

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    // 更新时间戳
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        this.timestamp = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "MonthlyDroneData{" +
                "id=" + id +
                ", droneCount=" + droneCount +
                ", illegalDroneCount=" + illegalDroneCount +
                ", disposedDroneCount=" + disposedDroneCount +
                ", yearMonth='" + yearMonth + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
