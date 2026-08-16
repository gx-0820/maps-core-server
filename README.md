# MAPS 低空防御指挥控制系统 · Core Server

MAPS Core Server 是低空防御指挥控制系统的后端核心服务，面向无人机等低空目标的实时感知、态势融合、设备联动、威胁评估与反制处置场景。

当前仓库主要承载业务编排、设备接入、算法结果接收、实时状态推送、权限管理、数据持久化以及自动反制策略等能力。系统通过 gRPC、WebSocket、HTTP/SSE 等方式连接算法模块、雷达/光电/诱骗等设备及前端态势中心，并使用 MySQL 与 Redis 管理历史数据和实时状态。

> 本仓库为系统后端工程。算法模型、前端应用以及部分设备侧程序不在本仓库范围内。

## 核心能力

### 1. 多源目标数据接入与业务联动

- 接收雷达、电侦、TDOA 等多类目标数据，并完成统一业务处理。
- 通过 gRPC Proto 定义与算法/设备服务进行结构化通信。
- 接收融合目标、轨迹预测等算法结果，并驱动后续目标展示、光电跟踪和反制流程。
- 支持 WebSocket、SSE 等实时推送方式，将目标态势和操作状态同步给前端。

### 2. 威胁评估与自动反制策略

系统包含配置驱动的自动反制模块，支持人工/自动模式切换，并将目标评估、策略选择、设备能力校验和指令执行拆分为独立服务。

主要能力包括：

- 自动处置轮次调度与动态周期刷新；
- RADAR / TDOA / FUSION 等目标源选择；
- 固定策略与自适应策略；
- 目标去重、白名单过滤、威胁排序与多目标规则；
- 连续高威胁目标升级策略；
- 干扰、诱骗驱离、捕获等处置动作；
- 设备目录与在线能力校验；
- 多厂商干扰设备协议适配；
- 当前处置动作统一停止与收口。

相关核心类位于：

```text
src/main/java/com/example/coreserver/service/countermeasure/
```

### 3. 异构设备接入

工程对雷达、光电、摄像头、诱骗及其他设备协议进行统一封装，设备侧通信实现分布在业务服务、Socket/gRPC 客户端和 WebSocket 服务模块中。

主要 Proto 定义：

```text
src/main/proto/
├── algorithm.proto
├── camera.proto
├── common.proto
├── config.proto
├── photoelectric.proto
├── radar.proto
├── talent.proto
└── uav.proto
```

### 4. 实时通信

- **gRPC**：用于后端与算法模块、部分设备服务之间的高频结构化通信。
- **WebSocket**：用于设备数据接入与实时数据转发。
- **SSE**：用于向前端持续推送处置状态、告警和业务事件。
- **MQTT**：用于部分外部设备/系统的数据交互。

### 5. 数据存储

- **MySQL**：保存用户、角色、权限、配置、历史目标数据、日志等持久化数据。
- **Redis**：保存实时状态、缓存和高频业务数据，降低关键链路中的同步数据库压力。
- **MyBatis-Plus / Spring Data JPA**：分别用于现有数据访问逻辑和 Repository 模式业务。

### 6. 安全与审计

- 基于 Spring Security + JWT 的身份认证；
- 用户、角色、权限关系管理；
- 接口级权限控制；
- 操作日志与异常日志记录；
- 面向设备操控和反制业务的权限约束。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 | Java 21 |
| Web 框架 | Spring Boot 3.4.3 |
| 安全 | Spring Security、JWT |
| 数据库 | MySQL |
| 缓存 | Redis |
| ORM / 数据访问 | Spring Data JPA、MyBatis-Plus |
| RPC | gRPC、Protocol Buffers |
| 实时通信 | WebSocket、SSE、MQTT |
| 空间计算 | JTS、Hibernate Spatial、JavaAPIforKml |
| 构建 | Maven |

## 项目结构

```text
maps-core-server/
├── ALife/                         # 部署/运行辅助脚本、SQL 与说明文档
├── src/
│   ├── main/
│   │   ├── java/com/example/coreserver/
│   │   │   ├── config/           # Spring、WebSocket、安全等配置
│   │   │   ├── controller/       # REST / SSE 接口
│   │   │   ├── entity/           # 领域实体
│   │   │   ├── repository/       # JPA Repository
│   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   ├── service/
│   │   │   │   ├── algorithm/    # 算法数据处理
│   │   │   │   ├── business/     # 设备/业务转换与编排
│   │   │   │   ├── countermeasure/# 自动反制策略与执行
│   │   │   │   ├── socket/       # Socket/gRPC 相关处理
│   │   │   │   └── threat/       # 威胁相关业务
│   │   │   ├── utils/            # 通用工具
│   │   │   └── wsserver/         # WebSocket 服务端相关实现
│   │   ├── proto/                # gRPC / Protobuf 接口定义
│   │   └── resources/            # 配置、Mapper XML、日志配置等
│   └── test/                      # 单元测试与 Spring Boot 集成测试
├── pom.xml
└── README.md
```

## 环境要求

建议准备以下运行环境：

- JDK 21
- Maven 3.9+
- MySQL 8.x
- Redis
- 与工程配置相匹配的算法服务和设备服务

## 本地启动

### 1. 初始化数据库

仓库 `ALife/sql/` 下提供了数据库结构/初始化 SQL，可根据实际环境选择对应脚本导入。

```text
ALife/sql/
├── core_server.sql
└── core_server.20260414.sql
```

### 2. 配置应用

根据运行环境修改：

```text
src/main/resources/application.yml
```

Docker 场景可参考：

```text
src/main/resources/application-docker.yml
```

需要重点确认：

- MySQL 地址、数据库名与账号；
- Redis 地址；
- gRPC 服务地址；
- WebSocket / MQTT 相关地址；
- JWT 等安全配置；
- 现场设备相关参数。

> 建议在实际部署环境中通过环境变量、外部配置文件或密钥管理服务注入凭据，不要在公开仓库中提交生产密码和密钥。

### 3. 编译

```bash
mvn clean package
```

### 4. 启动

```bash
mvn spring-boot:run
```

或运行打包后的 Jar：

```bash
java -jar target/core-server-0.0.1-SNAPSHOT.jar
```

## 测试

运行全部测试：

```bash
mvn test
```

当前测试重点覆盖自动反制配置、策略选择、动作执行、设备目录/协议适配以及部分 Controller 集成场景。

反制模块主要测试位于：

```text
src/test/java/com/example/coreserver/service/countermeasure/
```

## 主要业务链路

```text
雷达 / 电侦 / TDOA / 其他设备
              │
              ▼
      设备接入与数据标准化
              │
              ▼
       算法模块 / 数据融合
              │
              ▼
     融合目标与威胁评估结果
              │
      ┌───────┴────────┐
      ▼                ▼
实时态势推送       处置策略引擎
WebSocket / SSE        │
                       ▼
                设备能力与状态校验
                       │
                       ▼
              干扰 / 诱骗 / 捕获等动作
```

## 开发约定

仓库根目录的 `AGENTS.md` 记录了当前工程的开发与验证约定。进行功能修改时建议：

1. 优先复用现有 Controller / Service / Repository 分层；
2. 保持 Proto 接口兼容，避免业务层直接依赖算法实现细节；
3. 对设备控制、自动处置等高风险操作增加必要的权限、状态和参数校验；
4. 新增复杂业务逻辑时同步补充测试；
5. 不提交真实生产环境密钥、密码、Token 等敏感信息。

## API 文档

现有接口文档：

- Apifox：<https://apifox.com/apidoc/shared-82dc7d0a-dee3-44b9-abbd-824b4f1b6b54>

## License

当前仓库未声明开源许可证。未经项目所有者授权，请勿将代码、协议、配置或现场数据用于项目范围之外的用途。
