# 雷达目标与TDOA目标属性映射关系说明

## 📋 概述

本文档详细说明雷达探测目标实体类与 TDOA 目标实体类之间的属性映射关系，包括已映射字段、未映射字段以及数据类型转换规则。

---

## 🔄 已映射属性对照表

| 雷达字段 | 雷达类型 | TDOA字段 | TDOA类型 | 映射说明 | 示例值 |
|---------|---------|---------|---------|---------|--------|
| **基础标识** |
| `targetId` | Integer | `uavId` | String | 无人机唯一标识，格式：`RADAR_{targetId}` | `53` → `"RADAR_53"` |
| `targetBatch` | Long | `targetBatch` | Long | 设备原生目标批次号，直接映射 | `100L` |
| `deviceId` | String | `deviceId` / `sensorId` | String | 雷达设备编号，同时设置两个字段 | `"RADAR001"` |
| **时间信息** |
| `timestamp` | Date | `timestamp` | Date | 数据上报时间戳，直接映射 | `2026-05-11 10:00:00` |
| **位置信息（目标）** |
| `targetLon` | BigDecimal | `uavLon` | BigDecimal | 目标经度坐标（WGS84），直接映射 | `116.404` |
| `targetLat` | BigDecimal | `uavLat` | BigDecimal | 目标纬度坐标（WGS84），直接映射 | `39.915` |
| `altitude` | BigDecimal | `uavAlt` | BigDecimal | 目标海拔高度（米），直接映射 | `100.0` |
| **运动状态** |
| `speed` | BigDecimal | `velocity` | BigDecimal | 目标速度（米/秒），直接映射 | `15.5` |
| `xSpeed` + `ySpeed` + `zSpeed` | BigDecimal | `velocity` | BigDecimal | 三维速度分量，计算合速度作为备选 | `√(10²+8²+2²) ≈ 13.0` |
| **角度与距离** |
| `azimuth2` | BigDecimal | `uavAzimuth` | BigDecimal | 方位角（度），直接映射 | `45.0` |
| `range` | BigDecimal | `uavDistance` | BigDecimal | 目标距离（米），直接映射 | `1500.0` |
| `pitch` | BigDecimal | - | - | 俯仰角（度），TDOA无对应字段 | `10.0`（仅记录日志） |
| **目标分类** |
| `targetType` | Integer | `targetType` | Integer | 目标类型编码，直接映射 | `1` |
| **其他信息** |
| `protocolType` | String | - | - | 协议类型，TDOA无对应字段 | `"Protocol1"`（仅记录日志） |
| `snr` | BigDecimal | - | - | 信噪比，TDOA无对应字段 | `25.5`（仅记录日志） |

---

## ❌ 雷达有但 TDOA 无的属性

以下属性在雷达实体中存在，但在 TDOA 实体类中没有对应字段：

| 雷达字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `id` | String | UUID主键 | 由 MyBatis Plus 自动生成，转换时不使用 |
| `frameCount` | Integer | 数据帧计数 | 雷达数据帧序号 |
| `searchDirection` | BigDecimal | 搜索方向角度 | 雷达当前扫描方向 |
| `searchCycle` | Integer | 搜索周期 | 雷达扫描周期（秒） |
| `pulseGroupNumber` | Integer | 脉冲组号 | 雷达脉冲组标识 |
| `totalTargetCount` | Integer | 总目标数量 | 本次上报的目标总数 |
| `validTargetCount` | Integer | 有效目标数量 | 本次上报的有效目标数 |
| `isActive` | Integer | 激活状态 | 0-未激活 1-已激活 |
| `selectionFlag` | Integer | 目标选择标志 | 目标选择标记 |
| `isDelete` | Integer | 删除标记 | 0-正常 1-已删除 |
| `pitch` | BigDecimal | 俯仰角 | 目标的俯仰角度（度） |
| `protocolType` | String | 协议类型 | 雷达通信协议类型 |
| `snr` | BigDecimal | 信噪比 | 信号噪声比 |
| `xSpeed` | BigDecimal | X轴速度分量 | 三维速度的X分量 |
| `ySpeed` | BigDecimal | Y轴速度分量 | 三维速度的Y分量 |
| `zSpeed` | BigDecimal | Z轴速度分量 | 三维速度的Z分量 |

---

## ❌ TDOA 有但雷达无的属性

以下属性在 TDOA 实体类中存在，但雷达报文中没有对应数据：

### 飞行状态相关

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `uavModel` | String | 无人机型号 | 雷达无法识别具体型号 |
| `uavModelNo` | Integer | 无人机型号编号 | 需要额外查询或配置 |
| `userId` | String | 飞手注册编号 | 雷达无法获取飞手身份信息 |
| `traceId` | String | 轨迹追踪ID | 系统内部生成的追踪标识 |
| `uavHeight` | BigDecimal | 相对高度（米） | 相对于地面的高度 |
| `yaw` | BigDecimal | 偏航角（度） | 无人机朝向，雷达不提供 |
| `homeLon` | BigDecimal | 返航点经度 | 雷达无法获取返航点信息 |
| `homeLat` | BigDecimal | 返航点纬度 | 雷达无法获取返航点信息 |
| `startFrom` | Long | 飞行开始时间 | 毫秒时间戳 |
| `duration` | Integer | 飞行持续时间 | 秒为单位 |

### 位置信息（飞手/遥控器）

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `pilotLon` | BigDecimal | 飞手经度 | 雷达无法检测飞手位置 |
| `pilotLat` | BigDecimal | 飞手纬度 | 雷达无法检测飞手位置 |

### 区域与告警相关

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `areaFlag` | Integer | 区域标记 | 位掩码：1=探测区 2=警戒区 4=反制区 |
| `whiteListId` | Integer | 白名单ID | 数据库中的白名单记录ID |

### MQTT 与扩展信息

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `frequency` | Long | 数据上报频率 | 雷达不提供频率信息 |
| `sensorTopic` | String | MQTT消息主题 | 传感器数据发送目标 |
| `sensorLongitude` | BigDecimal | 传感器经度 | 需要从设备配置获取 |
| `sensorLatitude` | BigDecimal | 传感器纬度 | 需要从设备配置获取 |
| `sensorAltitude` | BigDecimal | 传感器海拔高度 | 需要从设备配置获取 |
| `deviceUuid` | String | 设备UUID | Extension.DeviceUUid |
| `extensionDeviceId` | String | 扩展设备ID | Extension.DeviceId |

### 系统字段

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `id` | String | UUID主键 | 由 MyBatis Plus 自动生成 |

---

## 🔧 数据类型转换规则

### 1. 直接映射（无需转换）

```java
// 大部分 BigDecimal 字段直接映射
tdoaTarget.setUavLon(radarTarget.getTargetLon());
tdoaTarget.setUavLat(radarTarget.getTargetLat());
tdoaTarget.setUavAlt(radarTarget.getAltitude());
tdoaTarget.setVelocity(radarTarget.getSpeed());
tdoaTarget.setUavAzimuth(radarTarget.getAzimuth2());
tdoaTarget.setUavDistance(radarTarget.getRange());
tdoaTarget.setTargetType(radarTarget.getTargetType());
```

### 2. 字符串拼接

```java
// targetId → uavId（添加前缀）
if (radarTarget.getTargetId() != null) {
    tdoaTarget.setUavId("RADAR_" + radarTarget.getTargetId());
}
```

### 3. 多对一映射

```java
// deviceId 同时设置到 deviceId 和 sensorId
if (radarTarget.getDeviceId() != null) {
    tdoaTarget.setDeviceId(radarTarget.getDeviceId());
    tdoaTarget.setSensorId(radarTarget.getDeviceId());
}
```

### 4. 计算映射（三维速度合成）

```java
// xSpeed, ySpeed, zSpeed → velocity（备选方案）
if (radarTarget.getXSpeed() != null && 
    radarTarget.getYSpeed() != null && 
    radarTarget.getZSpeed() != null) {
    
    double velocity = Math.sqrt(
        Math.pow(radarTarget.getXSpeed().doubleValue(), 2) +
        Math.pow(radarTarget.getYSpeed().doubleValue(), 2) +
        Math.pow(radarTarget.getZSpeed().doubleValue(), 2)
    );
    
    // 如果 speed 为空，使用计算值
    if (radarTarget.getSpeed() == null) {
        tdoaTarget.setVelocity(BigDecimal.valueOf(velocity));
    }
}
```

### 5. 默认值处理

```java
// timestamp 为空时使用当前时间
if (radarTarget.getTimestamp() != null) {
    tdoaTarget.setTimestamp(radarTarget.getTimestamp());
} else {
    tdoaTarget.setTimestamp(new Date());
}
```

### 6. 日志记录（不映射的字段）

```java
// protocolType 和 snr 仅记录日志
if (radarTarget.getProtocolType() != null) {
    log.debug("雷达协议类型: {}", radarTarget.getProtocolType());
}

if (radarTarget.getSnr() != null) {
    log.debug("雷达信噪比: {}", radarTarget.getSnr());
}
```

---

## 📊 映射统计

| 类别 | 数量 | 占比 |
|------|------|------|
| **已映射字段** | 14 | - |
| **雷达有但TDOA无** | 16 | - |
| **TDOA有但雷达无** | 22 | - |
| **雷达总字段数** | ~30 | 100% |
| **TDOA总字段数** | ~40 | 100% |
| **映射覆盖率** | ~47% | 雷达→TDOA |

---

## 💡 使用建议

### 1. 雷达特有数据处理

对于雷达报文中有但 TDOA 没有的字段：
- **运动学参数**（`xSpeed`, `ySpeed`, `zSpeed`）：可用于计算合速度或存储到扩展表
- **雷达状态**（`searchDirection`, `searchCycle`）：可用于雷达工作状态监控
- **信号质量**（`snr`）：可用于数据质量评估和过滤
- **俯仰角**（`pitch`）：可结合距离和高度进行三维定位验证

### 2. TDOA 缺失字段补充

对于 TDOA 有但雷达没有的字段：
- **无人机型号**（`uavModel`）：需要融合电侦或其他传感器数据
- **飞手位置**（`pilotLon/Lat`）：雷达无法检测，需其他数据源
- **传感器位置**（`sensorLongitude/Latitude/Altitude`）：从设备配置表获取
- **白名单信息**（`whiteListId`）：从数据库查询匹配

### 3. 数据质量检查

```java
// 检查必要字段
if (target.getUavId() == null || target.getUavId().isEmpty()) {
    log.warn("TDOA目标缺少无人机ID");
}

// 检查位置有效性
if (target.getUavLon() == null || target.getUavLat() == null) {
    log.warn("TDOA目标位置信息缺失");
}

// 检查速度合理性
if (target.getVelocity() != null && target.getVelocity().doubleValue() > 100) {
    log.warn("TDOA目标速度异常: {} m/s", target.getVelocity());
}
```

### 4. 多传感器融合建议

由于雷达和电侦提供的信息互补，建议：
- **雷达优势**：提供精确的距离、速度、方位角信息
- **电侦优势**：提供无人机型号、飞手位置、频率等信息
- **融合策略**：以 TDOA 为统一格式，通过 `uavId` 或时间窗口进行关联

---

## 🔍 与电侦转换的差异

| 对比项 | 电侦转换 | 雷达转换 |
|-------|---------|---------|
| **输入类型** | JSON 字符串 | Java 对象列表 |
| **解析方式** | 需要 JSON 解析 | 直接对象访问 |
| **返回类型** | List<DataTdoaTarget> | List<DataTdoaTarget> |
| **单个转换返回** | DataTdoaTarget | List<DataTdoaTarget> |
| **主要优势字段** | 型号、频率、飞手位置 | 速度、距离、方位角 |
| **缺失字段** | 速度、距离 | 型号、飞手位置、频率 |

---

## 📝 版本信息

- **文档版本**：v1.0
- **创建日期**：2026-05-11
- **适用实体**：
  - 雷达实体：`DataRadarTarget.java`
  - TDOA 实体：`DataTdoaTarget.java`

---

## 🔗 相关文件

- 转换工具类：[RadarToTdoaConverterService.java](file:///D:/ideaProject/MAPS-core-server/src/main/java/com/example/coreserver/service/business/RadarToTdoaConverterService.java)
- 测试类：[RadarToTdoaConverterServiceManualTest.java](file:///D:/ideaProject/MAPS-core-server/src/test/java/com/example/coreserver/service/business/RadarToTdoaConverterServiceManualTest.java)
- 雷达实体：[DataRadarTarget.java](file:///D:/ideaProject/MAPS-core-server/src/main/java/com/example/coreserver/entity/DataRadarTarget.java)
- TDOA 实体：[DataTdoaTarget.java](file:///D:/ideaProject/MAPS-core-server/src/main/java/com/example/coreserver/entity/DataTdoaTarget.java)
- 电侦转换工具：[ElectroToTdoaConverterService.java](file:///D:/ideaProject/MAPS-core-server/src/main/java/com/example/coreserver/service/business/ElectroToTdoaConverterService.java)
- 电侦映射文档：[电侦与TDOA目标属性映射说明.md](file:///D:/ideaProject/MAPS-core-server/电侦与TDOA目标属性映射说明.md)
