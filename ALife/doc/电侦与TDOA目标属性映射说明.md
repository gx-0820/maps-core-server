# 电侦目标与TDOA目标属性映射关系说明

## 📋 概述

本文档详细说明电侦探测目标报文与 TDOA 目标实体类之间的属性映射关系，包括已映射字段、未映射字段以及数据类型转换规则。

---

## 🔄 已映射属性对照表

| 电侦字段 | 电侦类型 | TDOA字段 | TDOA类型 | 映射说明 | 示例值 |
|---------|---------|---------|---------|---------|--------|
| **基础标识** |
| `id` / `rawID` | String | `uavId` | String | 无人机唯一标识，优先使用 rawID | `"F5YHX254J002MWHY"` |
| `model` | String | `uavModel` | String | 无人机型号 | `"DJI Mini3"` |
| `detectCounter` | Integer | `targetBatch` | Long | 设备原生目标批次号 | `1` → `1L` |
| **时间信息** |
| `detectTime` / `updateTime` | String (ISO 8601) | `timestamp` | Date | 数据上报时间戳，格式：`yyyy-MM-dd'T'HH:mm:ss.SSSSSS` | `"2026-05-10T23:13:35.399382"` |
| **位置信息（无人机）** |
| `lon` | Double | `uavLon` | BigDecimal | 无人机经度坐标（WGS84），值为 0.0 时忽略 | `116.404` |
| `lat` | Double | `uavLat` | BigDecimal | 无人机纬度坐标（WGS84），值为 0.0 时忽略 | `39.915` |
| `alt` | String | `uavAlt` | BigDecimal | 无人机海拔高度（米），"0.0" 时忽略 | `"100.0"` |
| **位置信息（飞手/遥控器）** |
| `rcLon` | Double | `pilotLon` | BigDecimal | 飞手经度坐标，值为 0.0 时忽略 | `116.403` |
| `rcLat` | Double | `pilotLat` | BigDecimal | 飞手纬度坐标，值为 0.0 时忽略 | `39.914` |
| **设备信息** |
| `details[0].deviceID` | String | `deviceId` | String | 设备编号（非数据库字段） | `"MD001"` |
| `details[0].deviceName` | String | `sensorId` | String | 传感器设备编号 | `"md_detect"` |
| `details[0].deviceLon` | Double | `sensorLongitude` | BigDecimal | 传感器经度坐标（WGS84） | `116.400` |
| `details[0].deviceLat` | Double | `sensorLatitude` | BigDecimal | 传感器纬度坐标（WGS84） | `39.910` |
| **频率与威胁** |
| `freq` | String | `frequency` | Long | 数据上报频率，移除小数点转换 | `"5796.5"` → `57965L` |
| `threat` | Integer | `targetType` | Integer | 目标类型编码，threat > 50 时为高威胁(1) | `100` → `1` |
| `type` | String | `targetType` | Integer | 目标类型，"drone" 映射为 1 | `"drone"` → `1` |

---

## ❌ 电侦有但 TDOA 无的属性

以下属性在电侦报文中存在，但在 TDOA 实体类中没有对应字段：

| 电侦字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `realID` | String | 真实ID | 与 rawID 相同，冗余字段 |
| `finder` | String | 发现者 | 空字符串，未使用 |
| `isRemoteID` | Boolean | 是否远程ID | 布尔值，表示是否为远程识别 |
| `iconUrl` | String | 图标URL | 空字符串，前端显示用 |
| `seenTimes` | Integer | 检测次数 | 累计检测次数 |
| `canSmartAttack` | Boolean | 可否智能攻击 | 布尔值 |
| `isSmartAttack` | Boolean | 是否智能攻击 | 布尔值 |
| `whiteListable` | Boolean | 可加入白名单 | 布尔值 |
| `whiteListName` | String | 白名单名称 | 如 "DJI Mini3" |
| `protocol` | String | 协议类型 | 如 "2/3" |
| `sharedNames` | Array | 共享名称列表 | 空数组 |
| `canIgnore` | Boolean | 可忽略 | 布尔值 |
| `isIgnored` | Boolean | 已忽略 | 布尔值 |
| `canPTZTo` | Boolean | 可云台跟踪 | 布尔值 |
| `details[0].targetID` | String | 目标ID | 与主ID相同 |
| `details[0].finder` | String | 发现者 | 空字符串 |
| `details[0].detectCounter` | Integer | 检测计数 | 通常为 0 |
| `details[0].azimuth` | null/Double | 方位角 | 通常为 null |
| `details[0].distance` | Double | 距离 | 通常为 0.0 |
| `details[0].updateTime` | String | 更新时间 | 与主 updateTime 相同 |

---

## ❌ TDOA 有但电侦无的属性

以下属性在 TDOA 实体类中存在，但电侦报文中没有对应数据：

### 飞行状态相关

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `uavModelNo` | Integer | 无人机型号编号 | 需要额外查询或配置 |
| `userId` | String | 飞手注册编号 | 电侦未提供飞手身份信息 |
| `traceId` | String | 轨迹追踪ID | 系统内部生成的追踪标识 |
| `uavHeight` | BigDecimal | 相对高度（米） | 相对于地面的高度 |
| `velocity` | BigDecimal | 速度（米/秒） | 电侦不提供速度信息 |
| `yaw` | BigDecimal | 偏航角（度） | 无人机朝向，0°=正北 |
| `homeLon` | BigDecimal | 返航点经度 | 电侦不提供返航点信息 |
| `homeLat` | BigDecimal | 返航点纬度 | 电侦不提供返航点信息 |
| `startFrom` | Long | 飞行开始时间 | 毫秒时间戳 |
| `duration` | Integer | 飞行持续时间 | 秒为单位 |

### 区域与告警相关

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `areaFlag` | Integer | 区域标记 | 位掩码：1=探测区 2=警戒区 4=反制区 |
| `whiteListId` | Integer | 白名单ID | 数据库中的白名单记录ID |

### MQTT 与扩展信息

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `sensorTopic` | String | MQTT消息主题 | 传感器数据发送目标 |
| `sensorAltitude` | BigDecimal | 传感器海拔高度 | 电侦 details 中未提供 |
| `uavAzimuth` | BigDecimal | 相对方位角 | 无人机相对传感器的方位角 |
| `uavDistance` | BigDecimal | 直线距离 | 无人机与传感器的直线距离（米） |
| `deviceUuid` | String | 设备UUID | Extension.DeviceUUid |
| `extensionDeviceId` | String | 扩展设备ID | Extension.DeviceId |

### 系统字段

| TDOA字段 | 类型 | 含义 | 说明 |
|---------|------|------|------|
| `id` | String | UUID主键 | 由 MyBatis Plus 自动生成 |

---

## 🔧 数据类型转换规则

### 1. 数值类型转换

```java
// Double → BigDecimal
BigDecimal uavLon = BigDecimal.valueOf(electroData.get("lon").asDouble());

// String → BigDecimal（高度）
BigDecimal uavAlt = new BigDecimal(electroData.get("alt").asText());

// Integer → Long（批次号）
Long targetBatch = (long) electroData.get("detectCounter").asInt();

// String → Long（频率，移除小数点）
String freqStr = electroData.get("freq").asText();
Long frequency = Long.parseLong(freqStr.replace(".", ""));
```

### 2. 时间格式转换

```java
// ISO 8601 格式 → Date
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
Date timestamp = sdf.parse(electroData.get("detectTime").asText());
```

### 3. 条件过滤规则

```java
// 经纬度为 0.0 时忽略（默认值）
if (lon != 0.0) {
    tdoaTarget.setUavLon(BigDecimal.valueOf(lon));
}

// 高度为 "0.0" 时忽略
if (!altStr.equals("0.0")) {
    tdoaTarget.setUavAlt(new BigDecimal(altStr));
}
```

### 4. 枚举映射规则

```java
// 目标类型映射
if ("drone".equals(type)) {
    tdoaTarget.setTargetType(1);  // 1 代表无人机
}

// 威胁等级映射
int threat = electroData.get("threat").asInt();
tdoaTarget.setTargetType(threat > 50 ? 1 : 0);  // >50 为高威胁
```

---

## 📊 映射统计

| 类别 | 数量 | 占比 |
|------|------|------|
| **已映射字段** | 18 | - |
| **电侦有但TDOA无** | 20 | - |
| **TDOA有但电侦无** | 22 | - |
| **电侦总字段数** | ~38 | 100% |
| **TDOA总字段数** | ~40 | 100% |
| **映射覆盖率** | ~47% | 电侦→TDOA |

---

## 💡 使用建议

### 1. 缺失数据处理

对于电侦报文中有但 TDOA 没有的字段：
- **业务无关字段**（如 `iconUrl`、`sharedNames`）：可以忽略
- **重要业务字段**（如 `isRemoteID`、`seenTimes`）：考虑是否需要扩展到 TDOA 实体或保存到扩展表

### 2. TDOA 缺失字段补充

对于 TDOA 有但电侦没有的字段：
- **计算得出**：如 `uavAzimuth`、`uavDistance` 可通过坐标计算
- **外部数据源**：如 `userId`、`homeLon/Lat` 需要从其他系统获取
- **系统生成**：如 `id`、`traceId` 由系统自动生成

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
```

---

## 📝 版本信息

- **文档版本**：v1.0
- **创建日期**：2026-05-11
- **适用实体**：
  - 电侦报文：原始 JSON 格式
  - TDOA 实体：`DataTdoaTarget.java`

---

## 🔗 相关文件

- 转换工具类：`TargetToTdoaConverterService.java`
- TDOA 实体：`DataTdoaTarget.java`
- 测试类：`TargetToTdoaConverterServiceManualTest.java`
