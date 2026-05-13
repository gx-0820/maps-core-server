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
 * 电侦目标全量数据表实体类
 */
@TableName(value = "data_elint_target")
@Data
public class DataElintTarget {
    /**
     * UUID主键，全局唯一标识符
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    /**
     * 数据上报时间戳（微秒精度），表分区依据字段
     */
    @JsonProperty("timestamp")
    @TableField("timestamp")
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    private Date timestamp;
    
    /**
     * 设备原生目标批次号
     */
    @JsonProperty("target_batch")
    @TableField("target_batch")
    private Long targetBatch;
    
    /**
     * 原始ID
     */
    @JsonProperty("rawID")
    @TableField("rawID")
    private String rawID;
    
    /**
     * 真实业务ID
     */
    @JsonProperty("realID")
    @TableField("realID")
    private String realID;
    
    /**
     * 目标类型 drone/rc
     */
    @JsonProperty("type")
    @TableField("type")
    private String type;
    
    /**
     * 探测源标识
     */
    @JsonProperty("finder")
    @TableField("finder")
    private String finder;
    
    /**
     * 是否远程ID
     */
    @JsonProperty("isRemoteID")
    @TableField("isRemoteID")
    private Boolean isRemoteID;
    
    /**
     * 探测次数
     */
    @JsonProperty("detectCounter")
    @TableField("detectCounter")
    private Integer detectCounter;
    
    /**
     * 无人机型号
     */
    @JsonProperty("model")
    @TableField("model")
    private String model;
    
    /**
     * 信号频率
     */
    @JsonProperty("freq")
    @TableField("freq")
    private String freq;
    
    /**
     * 威胁等级
     */
    @JsonProperty("threat")
    @TableField("threat")
    private Integer threat;
    
    /**
     * 图标地址
     */
    @JsonProperty("iconUrl")
    @TableField("iconUrl")
    private String iconUrl;
    
    /**
     * 持续发现次数
     */
    @JsonProperty("seenTimes")
    @TableField("seenTimes")
    private Integer seenTimes;
    
    /**
     * 经度
     */
    @JsonProperty("lon")
    @TableField("lon")
    private BigDecimal lon;
    
    /**
     * 纬度
     */
    @JsonProperty("lat")
    @TableField("lat")
    private BigDecimal lat;
    
    /**
     * 高度
     */
    @JsonProperty("alt")
    @TableField("alt")
    private String alt;
    
    /**
     * 遥控器经度
     */
    @JsonProperty("rcLon")
    @TableField("rcLon")
    private BigDecimal rcLon;
    
    /**
     * 遥控器纬度
     */
    @JsonProperty("rcLat")
    @TableField("rcLat")
    private BigDecimal rcLat;
    
    /**
     * 是否支持智能打击
     */
    @JsonProperty("canSmartAttack")
    @TableField("canSmartAttack")
    private Boolean canSmartAttack;
    
    /**
     * 是否正在智能打击
     */
    @JsonProperty("isSmartAttack")
    @TableField("isSmartAttack")
    private Boolean isSmartAttack;
    
    /**
     * 是否可加入白名单
     */
    @JsonProperty("whiteListable")
    @TableField("whiteListable")
    private Boolean whiteListable;
    
    /**
     * 白名单名称
     */
    @JsonProperty("whiteListName")
    @TableField("whiteListName")
    private String whiteListName;
    
    /**
     * 首次探测时间
     */
    @JsonProperty("detectTime")
    @TableField("detectTime")
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    private Date detectTime;
    
    /**
     * 最后更新时间
     */
    @JsonProperty("updateTime")
    @TableField("updateTime")
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    private Date updateTime;
    
    /**
     * 通信协议
     */
    @JsonProperty("protocol")
    @TableField("protocol")
    private String protocol;
    
    /**
     * 是否可忽略
     */
    @JsonProperty("canIgnore")
    @TableField("canIgnore")
    private Boolean canIgnore;
    
    /**
     * 是否已忽略
     */
    @JsonProperty("isIgnored")
    @TableField("isIgnored")
    private Boolean isIgnored;
    
    /**
     * 是否可云台联动
     */
    @JsonProperty("canPTZTo")
    @TableField("canPTZTo")
    private Boolean canPTZTo;
    
    /**
     * 关联目标ID
     */
    @JsonProperty("details_targetID")
    @TableField("details_targetID")
    private String detailsTargetID;
    
    /**
     * 探测设备ID
     */
    @JsonProperty("details_deviceID")
    @TableField("details_deviceID")
    private String detailsDeviceID;
    
    /**
     * 探测设备名称
     */
    @JsonProperty("details_deviceName")
    @TableField("details_deviceName")
    private String detailsDeviceName;
    
    /**
     * 探测设备经度
     */
    @JsonProperty("details_deviceLon")
    @TableField("details_deviceLon")
    private BigDecimal detailsDeviceLon;
    
    /**
     * 探测设备纬度
     */
    @JsonProperty("details_deviceLat")
    @TableField("details_deviceLat")
    private BigDecimal detailsDeviceLat;
    
    /**
     * 探测源
     */
    @JsonProperty("details_finder")
    @TableField("details_finder")
    private String detailsFinder;
    
    /**
     * 设备探测次数
     */
    @JsonProperty("details_detectCounter")
    @TableField("details_detectCounter")
    private Integer detailsDetectCounter;
    
    /**
     * 方位角
     */
    @JsonProperty("details_azimuth")
    @TableField("details_azimuth")
    private String detailsAzimuth;
    
    /**
     * 距离
     */
    @JsonProperty("details_distance")
    @TableField("details_distance")
    private BigDecimal detailsDistance;
    
    /**
     * 探测信息更新时间
     */
    @JsonProperty("details_updateTime")
    @TableField("details_updateTime")
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    private Date detailsUpdateTime;
}