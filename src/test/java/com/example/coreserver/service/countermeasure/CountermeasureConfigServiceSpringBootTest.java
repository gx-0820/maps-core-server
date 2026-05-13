package com.example.coreserver.service.countermeasure;

import com.example.coreserver.dto.countermeasure.CountermeasureStrategyProfile;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import com.example.coreserver.entity.countermeasure.CountermeasureExecutionScenario;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureTargetDataSource;
import com.example.coreserver.repository.ConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = CountermeasureConfigServiceSpringBootTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class CountermeasureConfigServiceSpringBootTest {

    private static final String LIVE_DB_MAP_CENTER = "114.427761, 22.700272";
    private static final String LIVE_DB_CAPTURE_POINT = "[114.425033,22.699680,20]";

    @Autowired
    private CountermeasureConfigService countermeasureConfigService;
    @Autowired
    private InMemoryConfigFixture configFixture;

    @BeforeEach
    void setUp() {
        configFixture.clear();
        countermeasureConfigService.ensureDefaults();
        configFixture.upsert(CountermeasureConfigService.MAP_CENTER_POINT_KEY, "地图中心点", LIVE_DB_MAP_CENTER);
        configFixture.upsert(CountermeasureConfigService.CAPTURE_POINT_KEY, "自动处置捕获点", LIVE_DB_CAPTURE_POINT);
    }

    @Test
    void shouldExposeDefaultCountermeasureConfigs() {
        CountermeasureStrategyProfile profile = countermeasureConfigService.getStrategyProfile();
        CountermeasureConfigService.GeoPoint centerPoint = countermeasureConfigService.getMapCenterPoint();
        CountermeasureConfigService.GeoPoint capturePoint = countermeasureConfigService.getCapturePoint();
        Config autoModeConfig = configFixture.getRequired(CountermeasureConfigService.AUTO_MODE_KEY);

        assertEquals(CountermeasureMode.MANUAL, countermeasureConfigService.getMode());
        assertEquals(1000L, countermeasureConfigService.getScanPeriodMs());
        assertEquals(CountermeasureTargetDataSource.FUSION, countermeasureConfigService.getTargetDataSource());
        assertEquals(CountermeasureExecutionScenario.PROD, countermeasureConfigService.getExecutionScenario());
        assertEquals("A", profile.getActivePreset());
        assertEquals("FIXED", profile.getActivePresetConfig().getMode());
        assertEquals(CountermeasureAction.DECEPTION_CAPTURE, profile.getPresets().get("B").getActionsForLevel("HIGH").getFirst());
        assertEquals(114.427761D, centerPoint.longitude(), 0.000001D);
        assertEquals(22.700272D, centerPoint.latitude(), 0.000001D);
        assertEquals(114.425033D, capturePoint.longitude(), 0.000001D);
        assertEquals(22.699680D, capturePoint.latitude(), 0.000001D);
        assertEquals(20.0D, capturePoint.altitude(), 0.000001D);
        assertNotNull(autoModeConfig.getConfigId());
        assertEquals("system", autoModeConfig.getCreateBy());
    }

    @Test
    void shouldPersistModeSwitchWithUpdaterMetadata() {
        Config before = configFixture.getRequired(CountermeasureConfigService.AUTO_MODE_KEY);

        countermeasureConfigService.setMode(CountermeasureMode.AUTO, "tester");

        Config after = configFixture.getRequired(CountermeasureConfigService.AUTO_MODE_KEY);
        assertEquals(before.getConfigId(), after.getConfigId());
        assertEquals("true", after.getConfigValue());
        assertEquals("tester", after.getUpdateBy());
        assertNotNull(after.getUpdateTime());
        assertEquals(CountermeasureMode.AUTO, countermeasureConfigService.getMode());
    }

    @Test
    void shouldKeepExistingCountermeasureConfigsWhenEnsuringDefaults() {
        configFixture.upsert(CountermeasureConfigService.AUTO_MODE_KEY, "自动处置模式", "true");
        configFixture.upsert(CountermeasureConfigService.SCAN_PERIOD_KEY, "自动处置扫描周期毫秒", "4321");
        configFixture.upsert(CountermeasureConfigService.TARGET_DATA_SOURCE_KEY, "自动处置目标来源", "TDOA");
        configFixture.upsert(CountermeasureConfigService.EXECUTION_SCENARIO_KEY, "自动处置场景", "DEBUG");
        configFixture.upsert(CountermeasureConfigService.STRATEGY_PROFILE_KEY, "自动处置策略配置",
                "{\"activePreset\":\"B\",\"presets\":{\"B\":{\"mode\":\"FIXED\",\"actions\":{\"LOW\":[\"NO_ACTION\"],\"MEDIUM\":[\"NO_ACTION\"],\"HIGH\":[\"DECEPTION_CAPTURE\"]},\"rules\":{}}}}");
        configFixture.upsert(CountermeasureConfigService.CAPTURE_POINT_KEY, "自动处置捕获点", "[114.400001,22.600002,88]");

        countermeasureConfigService.ensureDefaults();

        assertEquals(CountermeasureMode.AUTO, countermeasureConfigService.getMode());
        assertEquals(4321L, countermeasureConfigService.getScanPeriodMs());
        assertEquals(CountermeasureTargetDataSource.TDOA, countermeasureConfigService.getTargetDataSource());
        assertEquals(CountermeasureExecutionScenario.DEBUG, countermeasureConfigService.getExecutionScenario());
        assertEquals("B", countermeasureConfigService.getStrategyProfile().getActivePreset());
        assertEquals(114.400001D, countermeasureConfigService.getCapturePoint().longitude(), 0.000001D);
        assertEquals(22.600002D, countermeasureConfigService.getCapturePoint().latitude(), 0.000001D);
        assertEquals(88.0D, countermeasureConfigService.getCapturePoint().altitude(), 0.000001D);
    }

    @Test
    void shouldClampAndFallbackScanPeriod() {
        configFixture.upsert(CountermeasureConfigService.SCAN_PERIOD_KEY, "自动处置扫描周期毫秒", "50");
        assertEquals(200L, countermeasureConfigService.getScanPeriodMs());

        configFixture.upsert(CountermeasureConfigService.SCAN_PERIOD_KEY, "自动处置扫描周期毫秒", "not-a-number");
        assertEquals(1000L, countermeasureConfigService.getScanPeriodMs());
    }

    @Test
    void shouldFallbackToDefaultTargetSourceAndExecutionScenarioWhenConfigInvalid() {
        configFixture.upsert(CountermeasureConfigService.TARGET_DATA_SOURCE_KEY, "自动处置目标来源", "invalid-source");
        configFixture.upsert(CountermeasureConfigService.EXECUTION_SCENARIO_KEY, "自动处置场景", "invalid-scenario");

        assertEquals(CountermeasureTargetDataSource.FUSION, countermeasureConfigService.getTargetDataSource());
        assertEquals(CountermeasureExecutionScenario.PROD, countermeasureConfigService.getExecutionScenario());
    }

    @Test
    void shouldFallbackToDefaultStrategyProfileWhenStrategyJsonInvalid() {
        configFixture.upsert(CountermeasureConfigService.STRATEGY_PROFILE_KEY, "自动处置策略配置", "{\"activePreset\":");

        CountermeasureStrategyProfile profile = countermeasureConfigService.getStrategyProfile();

        assertEquals("A", profile.getActivePreset());
        assertEquals("ADAPTIVE", profile.getPresets().get("C").getMode());
        assertEquals(CountermeasureAction.UAV_ATTACK_AUTO,
                profile.getPresets().get("C").getRules().get("MULTI_TARGET").getActions().getFirst());
    }

    @Test
    void shouldReturnNullForInvalidMapCenterAndCapturePoint() {
        configFixture.upsert(CountermeasureConfigService.MAP_CENTER_POINT_KEY, "地图中心点", "114.42");
        configFixture.upsert(CountermeasureConfigService.CAPTURE_POINT_KEY, "自动处置捕获点", "[114.42,22.70]");

        assertNull(countermeasureConfigService.getMapCenterPoint());
        assertNull(countermeasureConfigService.getCapturePoint());

        configFixture.remove(CountermeasureConfigService.MAP_CENTER_POINT_KEY);
        assertNull(countermeasureConfigService.getMapCenterPoint());
    }

    static class InMemoryConfigFixture {
        private final Map<String, Config> configs = new ConcurrentHashMap<>();
        private final AtomicInteger nextId = new AtomicInteger(1);

        void clear() {
            configs.clear();
            nextId.set(1);
        }

        void upsert(String configKey, String configName, String configValue) {
            Config config = configs.getOrDefault(configKey, Config.builder().configKey(configKey).build());
            config.setConfigName(configName);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            configs.put(configKey, config);
        }

        void remove(String key) {
            configs.remove(key);
        }

        Optional<Config> find(String key) {
            return Optional.ofNullable(configs.get(key));
        }

        Config getRequired(String key) {
            return configs.get(key);
        }

        Config save(Config config) {
            if (config.getConfigId() == null) {
                config.setConfigId(nextId.getAndIncrement());
                if (config.getCreateTime() == null) {
                    config.setCreateTime(LocalDateTime.now());
                }
            }
            configs.put(config.getConfigKey(), config);
            return config;
        }
    }

    @SpringBootConfiguration
    @Import(CountermeasureConfigService.class)
    static class TestApplication {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        InMemoryConfigFixture inMemoryConfigFixture() {
            return new InMemoryConfigFixture();
        }

        @Bean
        ConfigRepository configRepository(InMemoryConfigFixture fixture) {
            ConfigRepository repository = Mockito.mock(ConfigRepository.class);
            when(repository.findByConfigKey(any())).thenAnswer(invocation -> fixture.find(invocation.getArgument(0)));
            when(repository.save(any())).thenAnswer(invocation -> fixture.save(invocation.getArgument(0)));
            return repository;
        }
    }
}
