package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 雷达目标全量数据表 - 存储雷达设备上报的目标探测时序数据
 * @TableName data_radar_target
 */
@TableName(value ="data_radar_target")
@Data
public class DataRadarTarget {
    /**
     * UUID主键，全局唯一标识符
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 数据上报时间戳（微秒精度），表分区依据字段
     */
    private Date timestamp;

    /**
     * 雷达设备编号（例：RADAR01）
     */
    private String deviceId;

    /**
     * 设备原生目标批次号
     */
    private Long targetBatch;

    /**
     * 设备原生目标ID（例：53/55/79）
     */
    private Integer targetId;

    /**
     * 协议类型（例：Protocol1）
     */
    private String protocolType;

    /**
     * 数据帧计数
     */
    private Integer frameCount;

    /**
     * 雷达搜索方向角度
     */
    private BigDecimal searchDirection;

    /**
     * 雷达搜索周期
     */
    private Integer searchCycle;

    /**
     * 脉冲组号
     */
    private Integer pulseGroupNumber;

    /**
     * 本次上报总目标数量
     */
    private Integer totalTargetCount;

    /**
     * 本次上报有效目标数量
     */
    private Integer validTargetCount;

    /**
     * 设备激活状态：0-未激活 1-已激活
     */
    private Integer isActive;

    /**
     * 信噪比
     */
    private BigDecimal snr;

    /**
     * 目标距离（米）
     */
    @TableField("`range`")
    private BigDecimal range;

    /**
     * 方位角
     */
    private BigDecimal azimuth2;

    /**
     * 俯仰角
     */
    private BigDecimal pitch;

    /**
     * 目标速度
     */
    private BigDecimal speed;

    /**
     * 目标高度
     */
    private BigDecimal altitude;

    /**
     * 目标纬度坐标
     */
    private BigDecimal targetLat;

    /**
     * 目标经度坐标
     */
    private BigDecimal targetLon;

    /**
     * 目标类型编码
     */
    private Integer targetType;

    /**
     * 目标选择标志
     */
    private Integer selectionFlag;

    /**
     * X轴方向速度分量
     */
    private BigDecimal xSpeed;

    /**
     * Y轴方向速度分量
     */
    private BigDecimal ySpeed;

    /**
     * Z轴方向速度分量
     */
    private BigDecimal zSpeed;

    /**
     * 数据状态标记：0-正常 1-已删除（仅标记不物理删除）
     */
    private Integer isDelete;
}