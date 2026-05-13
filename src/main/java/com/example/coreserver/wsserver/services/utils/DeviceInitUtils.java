package com.example.coreserver.wsserver.services.utils;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.coreserver.entity.Device;
import com.example.coreserver.entity.DeviceConf;
import com.example.coreserver.service.DeviceConfService;
import com.example.coreserver.service.DeviceDirectiveService;
import com.example.coreserver.service.DeviceManagerService;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.wsserver.WSConstant;
import com.example.coreserver.wsserver.services.CollectorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.hutool.core.thread.GlobalThreadPool.submit;

@Slf4j
@Service
public class DeviceInitUtils {

    private final DeviceManagerService deviceManagerService;
    private final DeviceConfService deviceConfService;
    private final CollectorConfig collectorConfig;
    private final ConfigService configService;

    private final String wsBaseUrl =  String.format("ws://%s:%s%s", "127.0.0.1", WSConstant.port, WSConstant.path);

    public DeviceInitUtils(DeviceManagerService deviceManagerService, DeviceConfService deviceConfService, CollectorConfig collectorConfig, ConfigService configService) {
        this.deviceManagerService = deviceManagerService;
        this.deviceConfService = deviceConfService;
        this.collectorConfig = collectorConfig;
        this.configService = configService;
    }


    /**
     * 1.启动读取当前所在区域的设备
     */
    public void loadAreaDevice() {
        List<Device> devices = getDevices()
                .stream()
                .filter(e -> StringUtils.isNotEmpty(e.getCollectFlag()))
                .toList();

        List<Long> deviceIds = devices.stream()
                .map(Device::getId)
                .collect(Collectors.toList());

        // 2.批量加载配置信息
        LambdaQueryWrapper<DeviceConf> queryWrapper = new LambdaQueryWrapper<DeviceConf>();
        queryWrapper.in(DeviceConf::getDeviceId, deviceIds);
        Map<Long, List<DeviceConf>> configMap = deviceConfService
                .list(queryWrapper)
                .stream()
                .collect(Collectors.groupingBy(DeviceConf::getDeviceId));

        // 3.并行处理设备（不阻塞主线程）
        List<CompletableFuture<Void>> futures = devices.stream()
                .map(device -> CompletableFuture.runAsync(() -> processDevice(device, configMap)))
                .toList();

        // 可选：等待所有设备启动完成（或设置超时）
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.error("部分设备启动失败", throwable);
                    return null;
                });
    }

    public @Nullable List<Device> getDevices() {
        String area = configService.getConfigValue("sys.location");

        // 1.加载系统部署所在地的设备
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getLocation, area);
        wrapper.orderByAsc(Device::getOrderNum);
        List<Device> devices = deviceManagerService.list(wrapper);

        if (devices.isEmpty()) {
            log.info("区域 [{}] 下没有可用的设备", area);
            return new ArrayList<>();
        }
        return devices;
    }

    /**
     * 处理单个设备的配置和启动
     */
    private void processDevice(Device device, Map<Long, List<DeviceConf>> configMap) {
        try {
            String deviceCode = device.getCollectFlag();
            Path deviceDir = Paths.get(collectorConfig.getPath(), deviceCode);

            // 1.生成配置文件
            Path configFile = deviceDir.resolve("config.json");
            String configJson = buildConfigJson(device, configMap);

            // 2.确保目录存在并写入配置
            ensureDirectoryExists(deviceDir);
            Files.writeString(configFile, configJson);
            log.info("配置文件已生成: {}", configFile);

            // 3.启动程序（非阻塞）
            startProgram(deviceDir, deviceCode);

        } catch (Exception e) {
            log.error("处理设备失败 - 设备ID: {}, 设备编码: {}",
                    device.getId(), device.getCollectFlag(), e);
        }
    }

    /**
     * 构建配置JSON
     */
    private String buildConfigJson(Device device, Map<Long, List<DeviceConf>> configMap) {
        JSONObject jsonObject = new JSONObject();

        // 添加设备配置
        List<DeviceConf> deviceConfs = configMap.get(device.getId());
        if (CollectionUtils.isNotEmpty(deviceConfs)) {
            deviceConfs.forEach(conf ->
                    jsonObject.put(conf.getConfKey(), conf.getConfValue())
            );
        }

        // 添加设备基本信息
        jsonObject.put("device_id", device.getId());
        jsonObject.put("type", device.getType());
        jsonObject.put("name", device.getName());
        jsonObject.put("device_code", device.getCollectFlag());

        // 添加WebSocket URI
        String wsUri = wsBaseUrl.replace("{code}", "");
        jsonObject.put("ws_uri", wsUri);

        // 可选：格式化的JSON（方便调试）
        return jsonObject.toJSONString();
    }

    /**
     * 确保目录存在
     */
    private void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("创建目录: {}", directory);
        }
    }

    /**
     * 启动程序（非阻塞）
     */
    private void startProgram(Path deviceDir, String deviceCode) {
        Path mainExe = deviceDir.resolve("main.exe");

        // 检查程序是否存在
        if (!Files.exists(mainExe) || !Files.isExecutable(mainExe)) {
            log.warn("程序不存在或不可执行: {}", mainExe);
            return;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(mainExe.toString());
            processBuilder.directory(deviceDir.toFile());
            processBuilder.redirectErrorStream(true);

            // 重定向输出到文件（可选）
//            Path logFile = deviceDir.resolve("output.log");
//            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                processBuilder.command("cmd", "/c", "start", mainExe.toString());
            } else {
                // Linux/Mac 系统
                processBuilder.command(mainExe.toString());
            }
            // 重定向输出流和错误流
            processBuilder.redirectErrorStream(true);

            // 启动进程，不等待
            Process process = processBuilder.start();

            // 可选：监控进程退出（使用独立线程）
            monitorProcess(process, deviceCode);

            log.info("程序已启动 - 设备: {}, PID: {}", deviceCode, process.pid());

        } catch (IOException e) {
            log.error("启动程序失败 - 设备: {}", deviceCode, e);
        }
    }

    /**
     * 监控进程退出
     */
    private void monitorProcess(Process process, String deviceCode) {
        submit(() -> {
            try {
                int exitCode = process.waitFor();
                log.info("程序退出 - 设备: {}, 退出码: {}", deviceCode, exitCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("进程监控被中断 - 设备: {}", deviceCode);
            }
        });
    }
}
