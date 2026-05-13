package com.example.coreserver.service.device;

import com.example.coreserver.entity.Config;
import com.example.coreserver.grpc.common.DeviceId;
import com.example.coreserver.grpc.common.Empty;
import com.example.coreserver.grpc.common.Response;
import com.example.coreserver.grpc.config.ConfigServiceGrpc;
import com.example.coreserver.grpc.config.Device;
import com.example.coreserver.grpc.config.DeviceConfig;
import com.example.coreserver.repository.ConfigRepository;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigService {

    @GrpcClient("device-server")
    private ConfigServiceGrpc.ConfigServiceBlockingStub stub;

    @Autowired
    private ConfigRepository configRepository;

    /**
     * 直接透传 device-server 的当前设备配置快照。
     */
    public DeviceConfig getConfig() {
        return stub.getConfig(Empty.newBuilder().build());
    }

    /**
     * 下面几组接口操作的是远端设备服务；
     * OFD 参数相关方法操作的是本地 config 表中的业务配置，两者不要混用。
     */
    public Response updateConfig(DeviceConfig request) {
        return stub.updateConfig(request);
    }

    public Response addDevices(DeviceConfig request) {
        return stub.addDevices(request);
    }

    public Response addDevice(Device request) {
        return stub.addDevice(request);
    }

    public Response removeDevice(DeviceId deviceId) {
        return stub.removeDevice(deviceId);
    }

    /**
     * 保存OFD参数到数据库
     * 
     * @param rangeDeviation 目标距离偏差
     * @param azimuthDeviation 目标方位角偏差
     * @param elevationDeviation 目标俯仰角偏差
     * @param updateBy 更新人
     * @return 保存结果的Map
     */
    public Map<String, Object> saveOFDParameters(String rangeDeviation, String azimuthDeviation, 
                                                   String elevationDeviation, String updateBy) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 保存目标距离偏差
            updateConfigValue("sys.OFD.rangeDDeviation", rangeDeviation, updateBy);
            // 保存目标方位角偏差
            updateConfigValue("sys.OFD.azimuthDeviation", azimuthDeviation, updateBy);
            // 保存目标俯仰角偏差
            updateConfigValue("sys.OFD.elevationDeviation", elevationDeviation, updateBy);

            result.put("success", true);
            result.put("message", "OFD参数保存成功");
            log.info("OFD parameters saved successfully: rangeDeviation={}, azimuthDeviation={}, elevationDeviation={}", 
                    rangeDeviation, azimuthDeviation, elevationDeviation);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "OFD参数保存失败: " + e.getMessage());
            log.error("Failed to save OFD parameters", e);
        }
        return result;
    }

    /**
     * 更新配置值
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @param updateBy 更新人
     */
    public void updateConfigValue(String configKey, String configValue, String updateBy) {
        Optional<Config> existingConfig = configRepository.findByConfigKey(configKey);
        if (existingConfig.isPresent()) {
            Config config = existingConfig.get();
            config.setConfigValue(configValue);
            config.setUpdateBy(updateBy);
            config.setUpdateTime(LocalDateTime.now());
            configRepository.save(config);
            log.info("Config updated: configKey={}, configValue={}", configKey, configValue);
        } else {
            // 这里只更新既有键，避免在设备参数保存接口里悄悄创建未知配置项。
            log.warn("Config not found for key: {}", configKey);
        }
    }

    /**
     * 获取OFD参数
     * 
     * @return OFD参数的Map
     */
    public Map<String, String> getOFDParameters() {
        Map<String, String> ofdParams = new HashMap<>();
        try {
            Optional<Config> rangeConfig = configRepository.findByConfigKey("sys.OFD.rangeDDeviation");
            Optional<Config> azimuthConfig = configRepository.findByConfigKey("sys.OFD.azimuthDeviation");
            Optional<Config> elevationConfig = configRepository.findByConfigKey("sys.OFD.elevationDeviation");

            ofdParams.put("rangeDeviation", rangeConfig.map(Config::getConfigValue).orElse("0"));
            ofdParams.put("azimuthDeviation", azimuthConfig.map(Config::getConfigValue).orElse("0"));
            ofdParams.put("elevationDeviation", elevationConfig.map(Config::getConfigValue).orElse("0"));

            log.info("OFD parameters retrieved: {}", ofdParams);
        } catch (Exception e) {
            log.error("Failed to get OFD parameters", e);
            // 返回默认值
            ofdParams.put("rangeDeviation", "0");
            ofdParams.put("azimuthDeviation", "0");
            ofdParams.put("elevationDeviation", "0");
        }
        return ofdParams;
    }

    /**
     * 获取特定的配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    public String getConfigValue(String configKey) {
        try {
            Optional<Config> config = configRepository.findByConfigKey(configKey);
            return config.map(Config::getConfigValue).orElse(null);
        } catch (Exception e) {
            log.error("Failed to get config value for key: {}", configKey, e);
            return null;
        }
    }


    /**
     * 获取特定的配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    public List<Config> getConfigKeys(List<String> configKey) {
        try {
            // 批量读取直接交给 repository，保持与现有调用方约定的返回结构一致。
            return configRepository.findByConfigKeys(configKey);
        } catch (Exception e) {
            log.error("Failed to get config value for key: {}", configKey, e);
            return null;
        }
    }
}
