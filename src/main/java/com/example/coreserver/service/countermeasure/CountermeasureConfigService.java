package com.example.coreserver.service.countermeasure;

import com.example.coreserver.dto.countermeasure.CountermeasureStrategyProfile;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.countermeasure.CountermeasureOmcFlag;
import com.example.coreserver.entity.countermeasure.CountermeasureExecutionScenario;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureTargetDataSource;
import com.example.coreserver.repository.ConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    public static final String OMC_FLAG_KEY = "sys.countermeasure.OMC_flag";
    public static final String LOCATION_KEY = "sys.location";
    public static final String JAMMING_FREQUENCY_KEY = "sys.EIS.frequency";
    public static final String MAP_CENTER_POINT_KEY = "sys.map.centerPoint";
    public static final String CAPTURE_POINT_KEY = "sys.SPOOFING.capturedCoordinate";

    private static final String AUTO_MODE_NAME = "自动处置模式";
    private static final String SCAN_PERIOD_NAME = "自动处置扫描周期毫秒";
    private static final String STRATEGY_PROFILE_NAME = "自动处置策略配置";
    private static final String TARGET_DATA_SOURCE_NAME = "自动处置目标来源";
    private static final String EXECUTION_SCENARIO_NAME = "自动处置场景";
    private static final String OMC_FLAG_NAME = "自动处置全向反制设备开启标识";
    private static final String CAPTURE_POINT_NAME = "导航诱骗设备定点捕获坐标";
    private static final long DEFAULT_SCAN_PERIOD_MS = 1000L;
    private static final long MIN_SCAN_PERIOD_MS = 200L;
    private static final String DEFAULT_UPDATE_BY = "system";
    private static final String DEFAULT_CAPTURE_POINT = "114.425033,22.699680,20";
    private static final String DEFAULT_JAMMING_FREQUENCY_CONFIG = "915MHz,1.5GHz,2.4GHz,5.8GHz";
    private static final CountermeasureTargetDataSource DEFAULT_TARGET_DATA_SOURCE = CountermeasureTargetDataSource.FUSION;
    private static final CountermeasureExecutionScenario DEFAULT_EXECUTION_SCENARIO = CountermeasureExecutionScenario.PROD;
    private static final CountermeasureOmcFlag DEFAULT_OMC_FLAG = CountermeasureOmcFlag.NONE;
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
        ensureDefaultConfigIfAbsent(OMC_FLAG_NAME, OMC_FLAG_KEY, DEFAULT_OMC_FLAG.name(), "自动处置全向反制设备开启标识");
        ensureDefaultConfigIfAbsent(CAPTURE_POINT_NAME, CAPTURE_POINT_KEY, DEFAULT_CAPTURE_POINT, "导航诱骗设备定点捕获坐标，经纬高逗号分隔");
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

    public CountermeasureOmcFlag getOmcFlag() {
        String raw = getConfigValue(OMC_FLAG_KEY).orElse(DEFAULT_OMC_FLAG.name());
        CountermeasureOmcFlag flag = CountermeasureOmcFlag.fromConfigValue(raw, DEFAULT_OMC_FLAG);
        if (!flag.name().equalsIgnoreCase(raw.trim())) {
            log.warn("自动处置全向设备开启标识配置无效，回退默认值: key={}, raw={}, fallback={}",
                    OMC_FLAG_KEY, raw, DEFAULT_OMC_FLAG);
        }
        return flag;
    }

    public void setOmcFlag(CountermeasureOmcFlag flag, String updateBy) {
        CountermeasureOmcFlag effectiveFlag = flag == null ? DEFAULT_OMC_FLAG : flag;
        log.info("更新自动处置全向设备开启标识: flag={}, updateBy={}", effectiveFlag, updateBy);
        upsertConfig(OMC_FLAG_NAME, OMC_FLAG_KEY, effectiveFlag.name(), "自动处置全向反制设备开启标识", updateBy);
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
     * 设置当前生效的反制策略。
     * <p>
     * 修改 {@code sys.countermeasure.strategy_profile} 配置 JSON 中 {@code activePreset} 的值，
     * 并校验目标策略必须存在于 {@code presets} 中。
     *
     * @param presetCode 策略编码，必须为 presets 中的已有键
     * @param updateBy   更新人
     * @throws IllegalArgumentException 策略编码不存在或配置缺失
     */
    public void setActivePreset(String presetCode, String updateBy) {
        String raw = getConfigValue(STRATEGY_PROFILE_KEY).orElse(DEFAULT_STRATEGY_PROFILE);
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(raw);
            JsonNode presetsNode = root.path("presets");
            if (presetsNode.isMissingNode() || !presetsNode.has(presetCode)) {
                throw new IllegalArgumentException("反制策略编码不存在: " + presetCode);
            }
            root.put("activePreset", presetCode);
            String updated = objectMapper.writeValueAsString(root);
            upsertConfig(STRATEGY_PROFILE_NAME, STRATEGY_PROFILE_KEY, updated, "自动处置策略 JSON", updateBy);
            log.info("反制策略已切换生效项: activePreset={}, updateBy={}", presetCode, updateBy);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新反制策略生效项失败: presetCode={}", presetCode, e);
            throw new IllegalStateException("更新反制策略生效项失败", e);
        }
    }

    /**
     * 修改指定反制策略的名称及各级威胁处置手段。
     * <p>
     * 仅允许修改已有策略的 {@code name} 和 {@code actions}（LOW / MEDIUM / HIGH），
     * 不允许新增或删除策略。
     *
     * @param presetCode 策略编码，必须为 presets 中的已有键
     * @param presetName 策略名称
     * @param low        低危处置手段，多个以逗号分隔
     * @param medium     中危处置手段，多个以逗号分隔
     * @param high       高危处置手段，多个以逗号分隔
     * @param updateBy   更新人
     * @throws IllegalArgumentException 策略编码不存在或配置缺失
     */
    public void updatePreset(String presetCode, String presetName, String low, String medium, String high, String updateBy) {
        String raw = getConfigValue(STRATEGY_PROFILE_KEY).orElse(DEFAULT_STRATEGY_PROFILE);
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(raw);
            JsonNode presetsNode = root.path("presets");
            if (presetsNode.isMissingNode() || !presetsNode.has(presetCode)) {
                throw new IllegalArgumentException("反制策略编码不存在: " + presetCode);
            }
            ObjectNode presetNode = (ObjectNode) presetsNode.path(presetCode);
            presetNode.put("name", presetName);
            // 更新 actions，若不存在则创建
            ObjectNode actionsNode = presetNode.has("actions") && presetNode.path("actions").isObject()
                    ? (ObjectNode) presetNode.path("actions")
                    : objectMapper.createObjectNode();
            actionsNode.set("LOW", parseActionArray(low));
            actionsNode.set("MEDIUM", parseActionArray(medium));
            actionsNode.set("HIGH", parseActionArray(high));
            presetNode.set("actions", actionsNode);
            String updated = objectMapper.writeValueAsString(root);
            upsertConfig(STRATEGY_PROFILE_NAME, STRATEGY_PROFILE_KEY, updated, "自动处置策略 JSON", updateBy);
            log.info("反制策略已修改: presetCode={}, presetName={}, low={}, medium={}, high={}, updateBy={}",
                    presetCode, presetName, low, medium, high, updateBy);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改反制策略失败: presetCode={}", presetCode, e);
            throw new IllegalStateException("修改反制策略失败", e);
        }
    }

    /**
     * 将逗号分隔的处置手段字符串解析为 JSON 数组节点。
     *
     * @param actions 逗号分隔的处置手段，如 "NO_ACTION" 或 "UAV_ATTACK_AUTO,DECEPTION_DRIVE"
     * @return ArrayNode
     */
    private ArrayNode parseActionArray(String actions) {
        ArrayNode array = objectMapper.createArrayNode();
        if (actions != null && !actions.isBlank()) {
            for (String part : actions.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    array.add(trimmed);
                }
            }
        }
        return array;
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

    public String getLocation() {
        return getConfigValue(LOCATION_KEY).map(String::trim).orElse(null);
    }

    public List<String> getJammingFrequencies() {
        String raw = getConfigValue(JAMMING_FREQUENCY_KEY).orElse(DEFAULT_JAMMING_FREQUENCY_CONFIG);
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .distinct()
                .toList();
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
     * 读取捕获点配置，格式为 "lon,lat,alt"。
     * 兼容历史方括号数组格式，且额外字段会被忽略。
     */
    public GeoPoint getCapturePoint() {
        String raw = getConfigValue(CAPTURE_POINT_KEY).orElse(DEFAULT_CAPTURE_POINT);
        try {
            String normalized = raw.trim();
            if (normalized.startsWith("[") && normalized.endsWith("]")) {
                normalized = normalized.substring(1, normalized.length() - 1);
            }
            String[] parts = normalized.split(",");
            if (parts.length < 3) {
                log.warn("自动处置捕获点配置格式不正确: key={}, raw={}", CAPTURE_POINT_KEY, raw);
                return null;
            }
            return new GeoPoint(
                    Double.parseDouble(parts[0].trim()),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim())
            );
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
