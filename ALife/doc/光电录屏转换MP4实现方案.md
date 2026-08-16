# 光电录屏 MP4 转换实现方案

## 背景

光电引导录屏文件会落到本地磁盘，并在 `core_server.photoelectric_file_record` 表生成记录。页面预览需要统一使用 MP4，因此服务端需要定时扫描未转换记录，将源视频转换为 MP4 后回写数据库。

需求文档中提到的字段名是 `core_path` / `core_path_mp4`，但当前开发库实际表结构为：

- `local_path`：源视频本地磁盘全路径
- `local_path_mp4`：转换后的 MP4 本地磁盘全路径

本次实现按当前项目和数据库实际字段落地。

## 配置项

配置位于 `src/main/resources/application.yml`：

```yaml
photoelectric-video:
  enabled: true
  scan-fixed-delay: 60000
  batch-size: 20
  delete-source-after-convert: false
  ffmpeg-path: tools/ffmpeg/ffmpeg.exe
  ffmpeg-timeout-minutes: 60
```

说明：

- `enabled`：是否启用转换任务。
- `scan-fixed-delay`：扫描间隔，单位毫秒。
- `batch-size`：每次扫描最多处理多少条记录。
- `delete-source-after-convert`：转换成功后是否删除源文件。
- `ffmpeg-path`：FFmpeg 可执行文件路径，支持相对项目根目录或绝对路径。
- `ffmpeg-timeout-minutes`：单个文件转换超时时间。

FFmpeg 可执行文件目录预留为 `tools/ffmpeg/`。仓库只保留 `.gitkeep`，实际 `ffmpeg.exe` 不纳入版本管理。

## 代码结构

新增类：

- `PhotoelectricVideoProperties`
  - 读取 `photoelectric-video` 配置。
- `PhotoelectricFileRecord`
  - 映射 `photoelectric_file_record` 表。
- `PhotoelectricFileRecordMapper`
  - 基于 MyBatis-Plus 的表访问入口。
- `PhotoelectricVideoConvertScheduler`
  - 独立调度器，使用 `SmartLifecycle` 启动。
  - 未启用全局 `@EnableScheduling`，避免激活项目中原本被注释掉的其他定时任务。
- `PhotoelectricVideoConvertService`
  - 查询待转换记录。
  - 调用 FFmpeg 转换。
  - 回写 `local_path_mp4`。
  - 按配置删除源文件。
  - 处理转换失败和半成品清理。

## 处理流程

1. 服务启动后，`PhotoelectricVideoConvertScheduler` 按 `scan-fixed-delay` 周期触发扫描。
2. 查询 `local_path` 不为空且 `local_path_mp4` 为空的记录。
3. 每条记录使用 MySQL `GET_LOCK` 获取按记录 ID 维度的转换锁，避免多实例重复转换。
4. 校验源文件：
   - 源路径为空：跳过并记录日志。
   - 源文件不存在：跳过并记录日志。
   - 源文件已是 `.mp4`：直接回写 `local_path_mp4`。
   - 仅支持 `.h264` 和 `.ts` 转换，其他格式暂跳过。
5. 输出路径默认使用源文件同目录、同文件名，仅后缀替换为 `.mp4`。
6. 使用 `ProcessBuilder(List<String>)` 执行 FFmpeg，避免命令字符串拼接。
7. 转换成功后检查目标 MP4 存在且大小大于 0，然后回写 `local_path_mp4`。
8. 如果 `delete-source-after-convert=true`，回写成功后删除源文件。
9. 转换失败时删除半成品 MP4，保留源文件。

## FFmpeg 命令

`.h264` 裸流转换：

```bash
ffmpeg -nostdin -y -fflags +genpts -f h264 -i input.h264 \
  -c:v libx264 -preset medium -pix_fmt yuv420p \
  -movflags +faststart -an -f mp4 output.mp4
```

`.ts` 转换：

```bash
ffmpeg -nostdin -y -i input.ts \
  -c:v libx264 -preset medium -pix_fmt yuv420p \
  -c:a aac -b:a 128k \
  -movflags +faststart -f mp4 output.mp4
```

## 失败处理

当前表没有转换状态字段，因此失败信息只写日志，不回写数据库。失败场景包括：

- FFmpeg 可执行文件不存在。
- 源文件不存在。
- 源格式不支持。
- FFmpeg 非 0 退出。
- FFmpeg 超时。
- 输出 MP4 未生成或大小为 0。

因为 `local_path_mp4` 不会被写入，失败记录会在后续扫描周期继续被发现。当前数据库中存在部分历史记录指向本机不存在的 `C:\h264\...` 文件，开发环境运行时会持续记录源文件不存在的 warning。

## 测试与验证

本次增加单元测试：

- `PhotoelectricVideoConvertServiceTest`
  - `.h264` 转换成功后写入 MP4 路径，默认保留源文件。
  - `.ts` 转换成功且开启删除开关时删除源文件。
  - FFmpeg 失败时删除半成品 MP4，不回写数据库。
  - 源文件已经是 `.mp4` 时直接回写源路径，不要求 FFmpeg 存在。
  - 校验 `.h264` 命令包含裸流输入参数。
  - 校验 `.ts` 命令包含音频 AAC 转码参数。

执行结果：

```bash
mvn -Dtest=PhotoelectricVideoConvertServiceTest test
```

结果：6 个测试全部通过。

FFmpeg 本地烟测：

- `tools/ffmpeg/ffmpeg.exe -version` 可正常执行。
- 使用 FFmpeg 生成 1 秒测试 `.h264` 文件后，再按服务同款命令转换为 `.mp4` 成功。
- 生成文件：
  - `target/codex-ffmpeg-smoke/sample.h264`
  - `target/codex-ffmpeg-smoke/sample.mp4`

## 后续建议

如果后续允许改表，建议增加转换状态字段，例如 `convert_status`、`convert_message`、`convert_retry_count`、`convert_start_time`、`convert_finish_time`。这样可以避免源文件缺失的历史记录被无限扫描，也便于页面或运维侧查看失败原因。
