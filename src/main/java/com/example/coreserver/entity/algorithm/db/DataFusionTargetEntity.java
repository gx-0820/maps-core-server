package com.example.coreserver.entity.algorithm.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 融合目标全量数据表映射。
 * 自动处置轮次任务会在时间窗内读取该表的目标作为输入源。
 */
@Data
@Entity
@IdClass(DataFusionTargetEntityId.class)
@Table(name = "data_fusion_target")
public class DataFusionTargetEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Id
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "target_batch", nullable = false)
    private Long targetBatch;

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Column(name = "radar_target_id", nullable = false, length = 64)
    private String radarTargetId;

    @Column(name = "tdoa_target_id", nullable = false, length = 64)
    private String tdoaTargetId;

    @Column(name = "range")
    private BigDecimal range;

    @Column(name = "azimuth")
    private BigDecimal azimuth;

    @Column(name = "pitch")
    private BigDecimal pitch;

    @Column(name = "speed")
    private BigDecimal speed;

    @Column(name = "altitude")
    private BigDecimal altitude;

    @Column(name = "target_lat")
    private BigDecimal targetLat;

    @Column(name = "target_lon")
    private BigDecimal targetLon;

    @Column(name = "target_type", length = 20)
    private String targetType;

    @Column(name = "frequency")
    private Long frequency;

    @Column(name = "start_from")
    private Long startFrom;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "uav_model", length = 128)
    private String uavModel;

    @Column(name = "white_list_id")
    private Integer whiteListId;
}
