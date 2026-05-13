package com.example.coreserver.utils;

import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.repository.ConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = ThreatAssessmentUtilSpringBootIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class ThreatAssessmentUtilSpringBootIntegrationTest {

    private static final double EPSILON = 1.0E-9D;

    private static final String COUNTERMEASURE_A = "[[108.7653000,34.0240000],[108.7663000,34.0240000],[108.7663000,34.0246000],[108.7653000,34.0246000],[108.7653000,34.0240000]]";
    private static final String WARNING_A = "[[108.7647000,34.0235000],[108.7669000,34.0235000],[108.7669000,34.0251000],[108.7647000,34.0251000],[108.7647000,34.0235000]]";
    private static final String DETECTION_A = "[[108.7640000,34.0229000],[108.7676000,34.0229000],[108.7676000,34.0257000],[108.7640000,34.0257000],[108.7640000,34.0229000]]";

    private static final String COUNTERMEASURE_B = "[[108.7668000,34.0248000],[108.7672000,34.0248000],[108.7672000,34.0252000],[108.7668000,34.0252000],[108.7668000,34.0248000]]";
    private static final String WARNING_B = "[[108.7644000,34.0232000],[108.7671000,34.0232000],[108.7671000,34.0253000],[108.7644000,34.0253000],[108.7644000,34.0232000]]";
    private static final String DETECTION_B = "[[108.7638000,34.0227000],[108.7677000,34.0227000],[108.7677000,34.0259000],[108.7638000,34.0259000],[108.7638000,34.0227000]]";
    private static final double COUNTERMEASURE_A_WGS_LON = 108.7608940160D;
    private static final double COUNTERMEASURE_A_WGS_LAT = 34.0257160779D;
    private static final double COUNTERMEASURE_B_WGS_LON = 108.7621942421D;
    private static final double COUNTERMEASURE_B_WGS_LAT = 34.0264166849D;
    private static final double OUTSIDE_WGS_LON = 108.7652947884D;
    private static final double OUTSIDE_WGS_LAT = 34.0315175949D;
    private static final double COUNTERMEASURE_A_FIRST_WGS_LON = 108.7605939771D;
    private static final double COUNTERMEASURE_B_FIRST_WGS_LON = 108.7620942260D;

    @Autowired
    private ThreatAssessmentUtil threatAssessmentUtil;

    @MockitoBean
    private ConfigRepository configRepository;

    @Test
    void shouldRunThreatAssessmentInsideSpringBootContext() {
        AtomicReference<List<Config>> configRef = new AtomicReference<>(configsA());
        mockThreatConfigs(configRef);

        ThreatAssessmentResult initialResult = threatAssessmentUtil.evaluate(
                buildArgs("2001", LocalDateTime.now(), COUNTERMEASURE_A_WGS_LON, COUNTERMEASURE_A_WGS_LAT, 120.0D, 8.0D)
        );
        assertNotNull(initialResult);
        assertEquals(ThreatAssessmentResult.ThreatAssessmentArea.COUNTERMEASURE, initialResult.getThreatAssessmentArea());
        assertEquals(ThreatAssessmentResult.ThreatLevel.HIGH, initialResult.getThreatLevel());
        assertEquals(COUNTERMEASURE_A_FIRST_WGS_LON, firstCountermeasureLongitude(threatAssessmentUtil), EPSILON);

        configRef.set(configsB());
        threatAssessmentUtil.refreshAreaConfigPeriodically();
        assertEquals(COUNTERMEASURE_B_FIRST_WGS_LON, firstCountermeasureLongitude(threatAssessmentUtil), EPSILON);

        ThreatAssessmentResult refreshedResult = threatAssessmentUtil.evaluate(
                buildArgs("2002", LocalDateTime.now().plusSeconds(1), COUNTERMEASURE_B_WGS_LON, COUNTERMEASURE_B_WGS_LAT, 120.0D, 8.0D)
        );
        assertNotNull(refreshedResult);
        assertEquals(ThreatAssessmentResult.ThreatAssessmentArea.COUNTERMEASURE, refreshedResult.getThreatAssessmentArea());
        assertEquals(ThreatAssessmentResult.ThreatLevel.HIGH, refreshedResult.getThreatLevel());
    }

    @Test
    void shouldReturnOutsideWhenPointIsOutsideClosedPolygons() {
        AtomicReference<List<Config>> configRef = new AtomicReference<>(configsA());
        mockThreatConfigs(configRef);

        ThreatAssessmentResult outsideResult = threatAssessmentUtil.evaluate(
                buildArgs("2003", LocalDateTime.now(), OUTSIDE_WGS_LON, OUTSIDE_WGS_LAT, 120.0D, 6.0D)
        );

        assertNotNull(outsideResult);
        assertEquals(ThreatAssessmentResult.ThreatAssessmentArea.OUTSIDE, outsideResult.getThreatAssessmentArea());
        assertEquals(ThreatAssessmentResult.ThreatLevel.NONE, outsideResult.getThreatLevel());
    }

    private void mockThreatConfigs(AtomicReference<List<Config>> configRef) {
        when(configRepository.findByConfigKeys(anyList())).thenAnswer(invocation -> configRef.get());
    }

    private static ThreatAssessmentArgs buildArgs(
            String id,
            LocalDateTime timestamp,
            double longitude,
            double latitude,
            double altitude,
            double speed
    ) {
        return ThreatAssessmentArgs.builder()
                .id(id)
                .timestamp(timestamp)
                .longitude(longitude)
                .latitude(latitude)
                .altitude(altitude)
                .speed(speed)
                .build();
    }

    private static List<Config> configsA() {
        return List.of(
                config("反制区坐标数组", "sys.zone.countermeasure", COUNTERMEASURE_A),
                config("预警区坐标数组", "sys.zone.warning", WARNING_A),
                config("探测区坐标数组", "sys.zone.detection", DETECTION_A)
        );
    }

    private static List<Config> configsB() {
        return List.of(
                config("反制区坐标数组", "sys.zone.countermeasure", COUNTERMEASURE_B),
                config("预警区坐标数组", "sys.zone.warning", WARNING_B),
                config("探测区坐标数组", "sys.zone.detection", DETECTION_B)
        );
    }

    private static Config config(String configName, String configKey, String configValue) {
        return Config.builder()
                .configName(configName)
                .configKey(configKey)
                .configValue(configValue)
                .build();
    }

    private static double firstCountermeasureLongitude(ThreatAssessmentUtil util) {
        try {
            Field snapshotField = ThreatAssessmentUtil.class.getDeclaredField("areaConfigSnapshot");
            snapshotField.setAccessible(true);
            Object snapshot = snapshotField.get(util);
            Method coordinatesMethod = snapshot.getClass().getDeclaredMethod("countermeasureCoordinates");
            coordinatesMethod.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<org.apache.commons.lang3.tuple.MutablePair<Double, Double>> coordinates =
                    (List<org.apache.commons.lang3.tuple.MutablePair<Double, Double>>) coordinatesMethod.invoke(snapshot);
            return coordinates.getFirst().left;
        } catch (Exception e) {
            throw new AssertionError("Failed to inspect countermeasure snapshot", e);
        }
    }

    @SpringBootConfiguration
    @Import(ThreatAssessmentUtil.class)
    static class TestApplication {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
