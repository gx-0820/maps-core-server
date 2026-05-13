package com.example.coreserver.service.countermeasure;

import com.example.coreserver.dto.countermeasure.CountermeasureStrategyProfile;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.countermeasure.CountermeasureExecutionScenario;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureTargetDataSource;
import com.example.coreserver.repository.ConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author codex
 * @description 自动处置配置服务 负责自动/人工模式、轮次周期、策略 JSON 以及地图中心点、捕获点等参数的读取。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountermeasureConfigService {

    public static final String AUTO_MODE_KEY = "sys.countermeasure.auto_mode";
    public static final String SCAN_PERIOD_KEY = "sys.countermeasure.scan_period_ms";
    public static final String STRATEGY_PROFILE_KEY = "sys.countermeasure.strategy_profile";
    public static final String TARGET_DATA_SOURCE_KEY = "sys.countermeasure.target_data_source";
    public static final String EXECUTION_SCENARIO_KEY = "sys.countermeasure.execution_scenario";
    public static final String MAP_CENTER_POINT_KEY = "sys.map.centerPoint";
    public static final String CAPTURE_POINT_KEY = "sys.countermeasure.capture_point";

    private static final String AUTO_MODE_NAME = "自动处置模式";
    private static final String SCAN_PERIOD_NAME = "自动处置扫描周期毫秒";
    private static final String STRATEGY_PROFILE_NAME = "自动处置策略配置";
    private static final String TARGET_DATA_SOURCE_NAME = "自动处置目标来源";
    private static final String EXECUTION_SCENARIO_NAME = "自动处置场景";
    private static final String CAPTURE_POINT_NAME = "自动处置捕获点";
    private static final long DEFAULT_SCAN_PERIOD_MS = 1000L;
    private static final long MIN_SCAN_PERIOD_MS = 200L;
    private static final String DEFAULT_UPDATE_BY = "system";
    private static final String DEFAULT_CAPTURE_POINT = "[114.425033,22.699680,20]";
    private static final CountermeasureTargetDataSource DEFAULT_TARGET_DATA_SOURCE = CountermeasureTargetDataSource.FUSION;
    private static final CountermeasureExecutionScenario DEFAULT_EXECUTION_SCENARIO = CountermeasureExecutionScenario.PROD;
    private static final String DEFAULT_STRATEGY_PROFILE = """
            {
              "activePreset": "A",
              "presets": {
                "A": {
                  "mode": "FIXED",
                  "actions": {
                    "LOW": ["NO_ACTION"],
                    "MEDIUM": ["UAV_ATTACK_AUTO"],
                    "HIGH": ["DECEPTION_DRIVE"]
                  },
                  "rules": {}
                },
                "B": {
                  "mode": "FIXED",
                  "actions": {
                    "LOW": ["UAV_ATTACK_AUTO"],
                    "MEDIUM": ["DECEPTION_DRIVE"],
                    "HIGH": ["DECEPTION_CAPTURE"]
                  },
                  "rules": {}
                },
                "C": {
                  "mode": "ADAPTIVE",
                  "actions": {
                    "LOW": ["DECEPTION_DRIVE"],
                    "MEDIUM": ["DECEPTION_DRIVE"],
                    "HIGH": ["DECEPTION_DRIVE"]
                  },
                  "rules": {
                    "MULTI_TARGET": {
                      "enabled": true,
                      "priority": 1,
                      "condition": {
                        "targetCountGte": 2
                      },
                      "actions": ["UAV_ATTACK_AUTO"]
                    },
                    "HIGH_DOMINANCE_UPGRADE": {
                      "enabled": true,
                      "priority": 2,
                      "condition": {
                        "threatLevel": "HIGH",
                        "scoreGapGte": 15,
                        "consecutiveRoundsGte": 2
                      },
                      "actions": ["DECEPTION_CAPTURE"]
                    }
                  }
                },
                "D": {
                  "mode": "RESERVED",
                  "actions": {
                    "LOW": ["NO_ACTION"],
                    "MEDIUM": ["NO_ACTION"],
                    "HIGH": ["NO_ACTION"]
                  },
                  "rules": {}
                }
              }
            }
            """;

    private final ConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void ensureDefaults() {
        log.info("开始校验自动处置默认配置是否齐全");
        ensureDefaultConfigIfAbsent(AUTO_MODE_NAME, AUTO_MODE_KEY, Boolean.toString(false), "自动/人工模式开关");
        ensureDefaultConfigIfAbsent(SCAN_PERIOD_NAME, SCAN_PERIOD_KEY, Long.toString(DEFAULT_SCAN_PERIOD_MS), "自动处置轮次周期，单位毫秒");
        ensureDefaultConfigIfAbsent(STRATEGY_PROFILE_NAME, STRATEGY_PROFILE_KEY, DEFAULT_STRATEGY_PROFILE, "自动处置策略 JSON");
        ensureDefaultConfigIfAbsent(TARGET_DATA_SOURCE_NAME, TARGET_DATA_SOURCE_KEY, DEFAULT_TARGET_DATA_SOURCE.name(), "自动处置目标数据来源:RADAR/TDOA/FUSION（雷达目标/TDOA目标/融合目标）");
        ensureDefaultConfigIfAbsent(EXECUTION_SCENARIO_NAME, EXECUTION_SCENARIO_KEY, DEFAULT_EXECUTION_SCENARIO.name(), "自动处置场景:DEBUG/PROD（DEBUG：开发场景，默认目标直接进入处置环节；PROD：按照实际业务逻辑逐步校验）");
        ensureDefaultConfigIfAbsent(CAPTURE_POINT_NAME, CAPTURE_POINT_KEY, DEFAULT_CAPTURE_POINT, "自动处置捕获点，经纬高 JSON 数组");
    }

    /**
     * 获取当前自动/人工模式。
     */
    public CountermeasureMode getMode() {
        return Boolean.parseBoolean(getConfigValue(AUTO_MODE_KEY).orElse(Boolean.FALSE.toString()))
                ? CountermeasureMode.AUTO
                : CountermeasureMode.MANUAL;
    }

    /**
     * 更新自动/人工模式。
     */
    public void setMode(CountermeasureMode mode, String updateBy) {
        boolean auto = CountermeasureMode.AUTO.equals(mode);
        log.info("更新自动处置模式: mode={}, updateBy={}", mode, updateBy);
        upsertConfig(AUTO_MODE_NAME, AUTO_MODE_KEY, Boolean.toString(auto), "自动/人工模式开关", updateBy);
    }

    /**
     * 获取自动处置轮次周期。
     */
    public long getScanPeriodMs() {
        try {
            long value = Long.parseLong(getConfigValue(SCAN_PERIOD_KEY).orElse(Long.toString(DEFAULT_SCAN_PERIOD_MS)));
            return Math.max(value, MIN_SCAN_PERIOD_MS);
        } catch (Exception e) {
            log.warn("自动处置扫描周期配置无效，回退默认值: key={}, error={}", SCAN_PERIOD_KEY, e.getMessage());
            return DEFAULT_SCAN_PERIOD_MS;
        }
    }

    /**
     * 读取自动处置策略 JSON。
     */
    public CountermeasureStrategyProfile getStrategyProfile() {
        String raw = getConfigValue(STRATEGY_PROFILE_KEY).orElse(DEFAULT_STRATEGY_PROFILE);
        try {
            return objectMapper.readValue(raw, CountermeasureStrategyProfile.class);
        } catch (Exception e) {
            log.error("自动处置策略配置解析失败，回退默认策略: key={}, error={}", STRATEGY_PROFILE_KEY, e.getMessage(), e);
            try {
                return objectMapper.readValue(DEFAULT_STRATEGY_PROFILE, CountermeasureStrategyProfile.class);
            } catch (Exception ex) {
                throw new IllegalStateException("内置自动处置策略配置解析失败", ex);
            }
        }
    }

    /**
     * 读取自动处置目标来源配置。
     */
    public CountermeasureTargetDataSource getTargetDataSource() {
        String raw = getConfigValue(TARGET_DATA_SOURCE_KEY).orElse(DEFAULT_TARGET_DATA_SOURCE.name());
        CountermeasureTargetDataSource source = CountermeasureTargetDataSource.fromConfigValue(raw, DEFAULT_TARGET_DATA_SOURCE);
        if (!source.name().equalsIgnoreCase(raw.trim())) {
            log.warn("自动处置目标来源配置无效，回退默认值: key={}, raw={}, fallback={}",
                    TARGET_DATA_SOURCE_KEY, raw, DEFAULT_TARGET_DATA_SOURCE);
        }
        return source;
    }

    /**
     * 读取自动处置执行场景配置。
     */
    public CountermeasureExecutionScenario getExecutionScenario() {
        String raw = getConfigValue(EXECUTION_SCENARIO_KEY).orElse(DEFAULT_EXECUTION_SCENARIO.name());
        CountermeasureExecutionScenario scenario = CountermeasureExecutionScenario.fromConfigValue(raw, DEFAULT_EXECUTION_SCENARIO);
        if (!scenario.name().equalsIgnoreCase(raw.trim())) {
            log.warn("自动处置场景配置无效，回退默认值: key={}, raw={}, fallback={}",
                    EXECUTION_SCENARIO_KEY, raw, DEFAULT_EXECUTION_SCENARIO);
        }
        return scenario;
    }

    /**
     * 读取地图中心点坐标，格式为 "lon,lat"。
     */
    public GeoPoint getMapCenterPoint() {
        Optional<String> raw = getConfigValue(MAP_CENTER_POINT_KEY);
        if (raw.isEmpty() || raw.get().isBlank()) {
            log.warn("地图中心点配置缺失: key={}", MAP_CENTER_POINT_KEY);
            return null;
        }
        try {
            String[] parts = raw.get().split(",");
            if (parts.length < 2) {
                log.warn("地图中心点配置格式不正确: key={}, raw={}", MAP_CENTER_POINT_KEY, raw.get());
                return null;
            }
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            return new GeoPoint(longitude, latitude, 0.0D);
        } catch (Exception e) {
            log.warn("地图中心点配置解析失败: key={}, error={}", MAP_CENTER_POINT_KEY, e.getMessage());
            return null;
        }
    }

    /**
     * 读取捕获点配置，格式为 [lon, lat, alt]。
     */
    public GeoPoint getCapturePoint() {
        String raw = getConfigValue(CAPTURE_POINT_KEY).orElse(DEFAULT_CAPTURE_POINT);
        try {
            List<Double> values = objectMapper.readValue(raw, new TypeReference<List<Double>>() {
            });
            if (values.size() < 3) {
                log.warn("自动处置捕获点配置格式不正确: key={}, raw={}", CAPTURE_POINT_KEY, raw);
                return null;
            }
            return new GeoPoint(values.get(0), values.get(1), values.get(2));
        } catch (Exception e) {
            log.warn("自动处置捕获点配置解析失败: key={}, error={}", CAPTURE_POINT_KEY, e.getMessage());
            return null;
        }
    }

    public Optional<String> getConfigValue(String key) {
        return configRepository.findByConfigKey(key).map(Config::getConfigValue);
    }

    private void upsertConfig(String name, String key, String value, String remark) {
        upsertConfig(name, key, value, remark, DEFAULT_UPDATE_BY);
    }

    private void ensureDefaultConfigIfAbsent(String name, String key, String value, String remark) {
        if (configRepository.findByConfigKey(key).isPresent()) {
            return;
        }
        log.info("写入缺失的自动处置默认配置: name={}, key={}, value={}", name, key, value);
        configRepository.save(buildConfigEntity(new Config(), name, key, value, remark, DEFAULT_UPDATE_BY));
    }

    private void upsertConfig(String name, String key, String value, String remark, String updateBy) {
        Optional<Config> existingConfig = configRepository.findByConfigKey(key);
        Config config = existingConfig.orElseGet(Config::new);
        configRepository.save(buildConfigEntity(config, name, key, value, remark, updateBy));
    }

    private Config buildConfigEntity(Config config, String name, String key, String value, String remark, String updateBy) {
        config.setConfigName(name);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType("Y");
        config.setRemark(remark);
        if (config.getConfigId() == null) {
            config.setCreateBy(updateBy);
        } else {
            config.setUpdateBy(updateBy);
            config.setUpdateTime(LocalDateTime.now());
        }
        return config;
    }

    public record GeoPoint(
            double longitude,
            double latitude,
            double altitude
    ) {
    }
}
