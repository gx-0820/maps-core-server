package com.example.coreserver.entity.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarData {
    @Schema(description = "设备ID")
    private String deviceId;                // 设备ID
    
    @Schema(description = "数据时间戳")
    private LocalDateTime timestamp;        // 数据时间戳
    
    @Schema(description = "协议类型")
    private String protocolType;           // 协议类型
    
    @Schema(description = "帧计数")
    private int frameCount;                // 帧计数
    
    @Schema(description = "搜索方位")
    private float searchDirection;         // 搜索方位
    
    @Schema(description = "搜索周期")
    private int searchCycle;              // 搜索周期
    
    @Schema(description = "当前脉冲组号")
    private int pulseGroupNumber;         // 当前脉冲组号
    
    @Schema(description = "目标列表")
    private List<RadarTarget> targets;    // 目标列表
    
    // 统计信息
    @Schema(description = "总目标数")
    private int totalTargetCount;         // 总目标数
    
    @Schema(description = "有效目标数（信噪比>0且航迹编号不为0）")
    private int validTargetCount;         // 有效目标数（信噪比>0且航迹编号不为0）
    
    // 设备状态
    @Schema(description = "设备是否活跃")
    private boolean isActive;             // 设备是否活跃
    
    @Schema(description = "设备状态描述")
    private String status;                // 设备状态描述
    
    // 扩展字段，用于存储其他可能需要的信息
    @Schema(description = "扩展信息")
    private String extendedInfo;          // 扩展信息

    private String type; // 添加 type 字段

}
