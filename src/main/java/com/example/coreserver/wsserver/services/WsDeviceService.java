package com.example.coreserver.wsserver.services;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coreserver.entity.Device;
import com.example.coreserver.entity.DeviceConf;
import com.example.coreserver.service.DeviceConfService;
import com.example.coreserver.service.DeviceDirectiveService;
import com.example.coreserver.service.DeviceManagerService;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.wsserver.WSConstant;
import com.example.coreserver.wsserver.services.utils.DeviceInitUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备操作类型
 * 1.启动读取当前 加载部署地区设备信息
 * 2.启动采集器
 * 3.接收数据
 * 4.下发指令
 * 5.。。
 *
 */
@Service
public class WsDeviceService {

    private final DeviceManagerService deviceManagerService;
    private final DeviceConfService deviceConfService;
    private final DeviceDirectiveService deviceDirectiveService;
    private final CollectorConfig collectorConfig;
    private final ConfigService configService;


    public WsDeviceService(DeviceManagerService deviceManagerService, DeviceConfService deviceConfService, DeviceDirectiveService deviceDirectiveService, CollectorConfig collectorConfig, ConfigService configService, DeviceInitUtils deviceInitUtils) {
        this.deviceManagerService = deviceManagerService;
        this.deviceConfService = deviceConfService;
        this.deviceDirectiveService = deviceDirectiveService;
        this.collectorConfig = collectorConfig;
        this.configService = configService;

        // 1.加载部署地区设备信息
        deviceInitUtils.loadAreaDevice();
    }


}
