package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 融合目标全量数据表
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("data_fusion_target")
public class DataFusionTarget {
    @TableId
    private String id;

    private Long targetBatch;

    private String targetId;
    private String radarTargetId;

    private String tdoaTargetId;

    private Date timestamp;

    @TableField("`range`")
    private BigDecimal range;

    private BigDecimal azimuth;

    private BigDecimal pitch;

    private BigDecimal speed;

    private BigDecimal altitude;

    private BigDecimal targetLat;

    private BigDecimal targetLon;

    private String targetType;

    private Long frequency;

    private Long startFrom;

    private Integer duration;

    private String uavModel;

    private Integer whiteListId;
}
