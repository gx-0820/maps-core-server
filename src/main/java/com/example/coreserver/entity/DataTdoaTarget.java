package com.example.coreserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * TDOA目标全量数据表 - 存储TDOA无人机探测系统上报的目标时序数据
 *
 * @TableName data_tdoa_target
 */
@TableName(value = "data_tdoa_target")
@Data
public class DataTdoaTarget {
    /**
     * UUID主键，全局唯一标识符
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**
     * 数据上报时间戳（微秒精度），表分区依据字段
     */
    @JsonProperty("timestamp")
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    private Date timestamp;

    @TableField(exist = false)
    @JsonProperty("device_id")
    private String deviceId;
    /**
     * 设备原生目标批次号
     */
    @JsonProperty("target_batch")
    private Long targetBatch;
    /**
     * 无人机唯一标识（序列号）
     */
    @JsonProperty("uav_id")
    private String uavId;
    /**
     * 无人机型号（例：DJI Mavic 3 Pro）
     */
    @JsonProperty("uav_model")
    private String uavModel;
    /**
     * 无人机型号编号
     */
    @JsonProperty("uav_model_no")
    private Integer uavModelNo;
    /**
     * 飞手注册编号（空表示未获取）
     */
    @JsonProperty("user_id")
    private String userId;
    /**
     * 轨迹追踪ID
     */
    @JsonProperty("trace_id")
    private String traceId;
    /**
     * 无人机经度坐标（WGS84坐标系）
     */
    @JsonProperty("uav_lon")
    private BigDecimal uavLon;
    /**
     * 无人机纬度坐标（WGS84坐标系）
     */
    @JsonProperty("uav_lat")
    private BigDecimal uavLat;
    /**
     * 无人机海拔高度（米）
     */
    @JsonProperty("uav_alt")
    private BigDecimal uavAlt;
    /**
     * 无人机相对高度（米）
     */
    @JsonProperty("uav_height")
    private BigDecimal uavHeight;
    /**
     * 无人机速度（米/秒）
     */
    @JsonProperty("velocity")
    private BigDecimal velocity;
    /**
     * 无人机偏航角（度，正北为0°，顺时针360°）
     */
    @JsonProperty("yaw")
    private BigDecimal yaw;
    /**
     * 飞手经度坐标
     */
    @JsonProperty("pilot_lon")
    private BigDecimal pilotLon;
    /**
     * 飞手纬度坐标
     */
    @JsonProperty("pilot_lat")
    private BigDecimal pilotLat;
    /**
     * 返航点经度坐标
     */
    @JsonProperty("home_lon")
    private BigDecimal homeLon;
    /**
     * 返航点纬度坐标
     */
    @JsonProperty("home_lat")
    private BigDecimal homeLat;
    /**
     * 本次飞行开始时间（毫秒时间戳）
     */
    @JsonProperty("start_from")
    private Long startFrom;
    /**
     * 飞行持续时间（秒）
     */
    @JsonProperty("duration")
    private Integer duration;
    /**
     * 数据上报频率（Hz）
     */
    @JsonProperty("frequency")
    private Long frequency;
    /**
     * 区域标记（位掩码）：1=探测区 2=警戒区 4=反制区（可叠加）
     */
    @JsonProperty("area_flag")
    private Integer areaFlag;
    /**
     * 白名单ID（在白名单中的无人机会标记此值）
     */
    @JsonProperty("white_list_id")
    private Integer whiteListId;
    /**
     * 目标类型编码
     */
    @JsonProperty("target_type")
    private Integer targetType;
    /**
     * MQTT消息主题（传感器数据发送目标）
     */
    @JsonProperty("sensor_topic")
    private String sensorTopic;
    /**
     * 传感器设备编号
     */
    @JsonProperty("sensor_id")
    private String sensorId;
    /**
     * 传感器经度坐标（WGS84）
     */
    @JsonProperty("sensor_longitude")
    private BigDecimal sensorLongitude;
    /**
     * 传感器纬度坐标（WGS84）
     */
    @JsonProperty("sensor_latitude")
    private BigDecimal sensorLatitude;
    /**
     * 传感器海拔高度（米）
     */
    @JsonProperty("sensor_altitude")
    private BigDecimal sensorAltitude;
    /**
     * 无人机相对传感器方位角（度，0°=正北 90°=正东 180°=正南 270°=正西）
     */
    @JsonProperty("uav_azimuth")
    private BigDecimal uavAzimuth;
    /**
     * 无人机与传感器直线距离（米）
     */
    @JsonProperty("uav_distance")
    private BigDecimal uavDistance;
    /**
     * 设备UUID（Extension.DeviceUUid）
     */
    @JsonProperty("device_uuid")
    private String deviceUuid;

    /**
     * 扩展设备ID（Extension.DeviceId）
     */
    @JsonProperty("extension_device_id")
    private String extensionDeviceId;
}