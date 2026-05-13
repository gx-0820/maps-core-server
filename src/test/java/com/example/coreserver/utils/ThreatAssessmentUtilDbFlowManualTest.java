package com.example.coreserver.utils;

import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.repository.ConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = ThreatAssessmentUtilDbFlowManualTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class ThreatAssessmentUtilDbFlowManualTest {

    private static final List<String> THREAT_CONFIG_KEYS = List.of(
            "sys.zone.countermeasure",
            "sys.zone.warning",
            "sys.zone.detection"
    );

    @Autowired
    private ThreatAssessmentUtil threatAssessmentUtil;

    @Autowired
    private ConfigRepository configRepository;

    @Test
    void shouldEvaluateThreatAssessmentArgsUsingRealDatabaseConfig() throws Exception {
        List<Config> configs = configRepository.findByConfigKeys(THREAT_CONFIG_KEYS);
        assertFalse(configs.isEmpty(), "Expected threat assessment configs to be loaded from database");

        ThreatAssessmentArgs args = buildArgs();
        ThreatAssessmentResult result = threatAssessmentUtil.evaluate(args);
        assertNotNull(result);

        System.out.println("==== ThreatAssessment DB Flow Test ====");
        System.out.println("Loaded configs from database:");
        for (Config config : configs) {
            System.out.printf("%s = %s%n", config.getConfigKey(), config.getConfigValue());
        }
        System.out.println("Input ThreatAssessmentArgs: " + args);
        System.out.println("ThreatAssessmentResult: " + result);
    }

    private static ThreatAssessmentArgs buildArgs() {
        return ThreatAssessmentArgs.builder()
                .id("2043639702413754368")
                .targetType(ThreatAssessmentArgs.TargetType.RADAR)
                .timestamp(LocalDateTime.parse("2026-04-13T11:48:54.784894300"))
                .imageTransmission(false)
                .whiteList(false)
                .speed(-6.950571060180664)
//                .longitude(114.43770167602)
//                .latitude(22.6952683065941)
                .longitude(114.42821001489219)
                .latitude(22.70178032846747)
                .altitude(91.48104858398438)
                .build();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Config.class)
    @EnableJpaRepositories(basePackageClasses = ConfigRepository.class)
    @Import({ThreatAssessmentUtil.class, TestConfig.class})
    static class TestApplication {
    }
}
