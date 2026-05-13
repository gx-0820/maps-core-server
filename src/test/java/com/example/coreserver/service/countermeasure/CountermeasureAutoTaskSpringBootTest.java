package com.example.coreserver.service.countermeasure;

import com.example.coreserver.controller.CountermeasureController;
import com.example.coreserver.controller.OperationSseController;
import com.example.coreserver.dto.ModeUpdateRequest;
import com.example.coreserver.dto.countermeasure.CountermeasureStrategyProfile;
import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.entity.algorithm.db.DataFusionTargetEntity;
import com.example.coreserver.entity.countermeasure.CountermeasureAction;
import com.example.coreserver.entity.countermeasure.CountermeasureExecutionScenario;
import com.example.coreserver.entity.countermeasure.CountermeasureMode;
import com.example.coreserver.entity.countermeasure.CountermeasureTargetDataSource;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.grpc.config.Device;
import com.example.coreserver.grpc.config.DeviceConfig;
import com.example.coreserver.grpc.talent.CaptureRequest;
import com.example.coreserver.grpc.talent.ConnectionStatus;
import com.example.coreserver.grpc.talent.DriveAngleRequest;
import com.example.coreserver.grpc.talent.PositionRequest;
import com.example.coreserver.grpc.talent.TransmitPowerRequest;
import com.example.coreserver.grpc.uav.AttackAutoRequest;
import com.example.coreserver.mapper.DataRadarTargetMapper;
import com.example.coreserver.mapper.DataTdoaTargetMapper;
import com.example.coreserver.repository.ConfigRepository;
import com.example.coreserver.repository.algorithm.DataFusionTargetRepository;
import com.example.coreserver.service.device.ConfigService;
import com.example.coreserver.service.device.TalentService;
import com.example.coreserver.service.device.UavService;
import com.example.coreserver.service.impl.CountermeasureServiceImpl;
import com.example.coreserver.utils.ThreatAssessmentUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = CountermeasureAutoTaskSpringBootTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class CountermeasureAutoTaskSpringBootTest {

    private static final long TEST_SCAN_PERIOD_MS = 5000L;
    private static final String LIVE_DB_MAP_CENTER = "114.427761, 22.700272";
    private static final String LIVE_DB_CAPTURE_POINT = "[114.425033,22.699680,20]";
    private static final ConnectionStatus CONNECTED_STATUS = ConnectionStatus.newBuilder().setConnected(true).build();

    @Autowired
    private CountermeasureAutoTaskService autoTaskService;
    @Autowired
    private CountermeasureConfigService countermeasureConfigService;
    @Autowired
    private CountermeasureController countermeasureController;
    @Autowired
    private InMemoryConfigFixture configFixture;
    @Autowired
    private InMemoryFusionTargetFixture fusionTargetFixture;
    @Autowired
    private InMemoryRadarTargetFixture radarTargetFixture;
    @Autowired
    private InMemoryTdoaTargetFixture tdoaTargetFixture;
    @Autowired
    private ThreatAssessmentUtil threatAssessmentUtil;
    @Autowired
    private DataFusionTargetRepository dataFusionTargetRepository;
    @Autowired
    private DataRadarTargetMapper dataRadarTargetMapper;
    @Autowired
    private DataTdoaTargetMapper dataTdoaTargetMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private TalentService talentService;
    @Autowired
    private UavService uavService;

    @BeforeEach
    void setUp() throws Exception {
        autoTaskService.stopCurrentIntervention("test setup reset");
        configFixture.clear();
        fusionTargetFixture.clear();
        radarTargetFixture.clear();
        tdoaTargetFixture.clear();
        reset(threatAssessmentUtil, configService, talentService, uavService);

        countermeasureConfigService.ensureDefaults();
        configFixture.upsert(CountermeasureConfigService.MAP_CENTER_POINT_KEY, "地图中心点", LIVE_DB_MAP_CENTER);
        configFixture.upsert(CountermeasureConfigService.CAPTURE_POINT_KEY, "自动处置捕获点", LIVE_DB_CAPTURE_POINT);

        setDevices(electricDevice("E01"), talentDevice("T01"));
        when(talentService.isConnected(any())).thenReturn(CONNECTED_STATUS);
    }

    @AfterEach
    void tearDown() {
        autoTaskService.stopCurrentIntervention("test teardown");
    }

    @Test
    void shouldSkipImmediateRoundWhenModeIsManualInConfig() {
        configFixture.upsert(CountermeasureConfigService.AUTO_MODE_KEY, "自动处置模式", "false");

        autoTaskService.triggerImmediateRound();

        verify(uavService, after(500).never()).setAttackAuto(any());
        verify(talentService, after(500).never()).sendTransmitPowerCommand(any());
    }

    @Test
    void shouldDetectAutoModeSwitchFromConfigPollingWithoutManualTrigger() {
        clearInvocations(dataFusionTargetRepository);

        configFixture.upsert(CountermeasureConfigService.SCAN_PERIOD_KEY, "自动处置扫描周期毫秒", "200");
        configFixture.upsert(CountermeasureConfigService.AUTO_MODE_KEY, "自动处置模式", "true");

        verify(dataFusionTargetRepository, timeout(1500)).findByTimestampBetweenOrderByTimestampDesc(any(), any());
    }

    @Test
    void shouldKeepOnlyLatestUniqueNonWhitelistTargetsAfterFiltering() {
        enableAutoMode();
        useStrategy(fixedStrategyA());

        LocalDateTime now = LocalDateTime.now();
        DataFusionTargetEntity latest = buildTarget("dup", now, "114.430000", "22.705000", "120.0", "800.0", "6.5");
        DataFusionTargetEntity older = buildTarget("dup", now.minusSeconds(1), "114.420000", "22.704000", "110.0", "700.0", "6.0");
        DataFusionTargetEntity whitelist = buildTarget("white", now, "114.431000", "22.706000", "90.0", "600.0", "5.0");
        whitelist.setWhiteListId(7);
        DataFusionTargetEntity missingLon = buildTarget("missing-lon", now, "114.432000", "22.707000", "100.0", "500.0", "4.0");
        missingLon.setTargetLon(null);
        DataFusionTargetEntity broken = buildTarget("broken", now, "114.499000", "22.708000", "80.0", "400.0", "3.0");
        fusionTargetFixture.replace(List.of(older, latest, whitelist, missingLon, broken));

        when(threatAssessmentUtil.evaluate(any())).thenAnswer(invocation -> {
            ThreatAssessmentArgs args = invocation.getArgument(0);
            if (Math.abs(args.getLongitude() - 114.499000D) < 0.000001D) {
                throw new RuntimeException("mock threat assessment failure");
            }
            return ThreatAssessmentResult.builder()
                    .threatLevel(ThreatAssessmentResult.ThreatLevel.LOW)
                    .threatScore(12)
                    .whiteList(args.isWhiteList())
                    .build();
        });

        autoTaskService.triggerImmediateRound();

        verify(threatAssessmentUtil, timeout(3000).times(3)).evaluate(any());
        verify(uavService, after(800).never()).setAttackAuto(any());
        verify(talentService, after(800).never()).sendTransmitPowerCommand(any());
        assertEquals(1, autoTaskService.getCurrentRoundTargetCache().size());
        assertTrue(autoTaskService.getCurrentRoundTargetCache().containsKey("0_dup"));
        assertEquals(now, autoTaskService.getCurrentRoundTargetCache().get("0_dup").timestamp());
    }

    @Test
    void shouldLoadTdoaTargetsWhenTargetDataSourceConfiguredToTdoa() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        useTargetDataSource(CountermeasureTargetDataSource.TDOA);
        tdoaTargetFixture.replace(List.of(buildTdoaTarget(
                "tdoa-row-1",
                LocalDateTime.now(),
                "UAV-001",
                "TRACE-001",
                "114.430000",
                "22.705000",
                "120.0",
                "800.0",
                "12.5",
                "98.5",
                0
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.MEDIUM, 72));

        autoTaskService.triggerImmediateRound();

        verify(dataTdoaTargetMapper, timeout(3000).atLeastOnce()).selectList(any());
        verify(uavService, timeout(3000)).setAttackAuto(Mockito.argThat((AttackAutoRequest request) ->
                "E01".equals(request.getDeviceId()) && !request.getIsCancel()
        ));
        assertTrue(autoTaskService.getCurrentRoundTargetCache().containsKey("UAV-001_TRACE-001"));
    }

    @Test
    void shouldLoadRadarTargetsWhenTargetDataSourceConfiguredToRadar() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        useTargetDataSource(CountermeasureTargetDataSource.RADAR);
        radarTargetFixture.replace(List.of(buildRadarTarget(
                "radar-row-1",
                LocalDateTime.now(),
                77L,
                8,
                "114.430000",
                "22.705000",
                "120.0",
                "800.0",
                "6.5",
                "18.0"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 88));

        autoTaskService.triggerImmediateRound();

        verify(dataRadarTargetMapper, timeout(3000)).selectList(any());
        verify(talentService, timeout(3000)).sendDriveAngleCommand(any());
        assertTrue(autoTaskService.getCurrentRoundTargetCache().containsKey("77_8"));
    }

    @Test
    void shouldTreatZeroWhiteListIdAsNonWhitelistForTdoaTarget() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        useTargetDataSource(CountermeasureTargetDataSource.TDOA);
        tdoaTargetFixture.replace(List.of(buildTdoaTarget(
                "tdoa-row-zero-white",
                LocalDateTime.now(),
                "UAV-002",
                "TRACE-002",
                "114.431000",
                "22.706000",
                "110.0",
                "700.0",
                "10.0",
                "45.0",
                0
        )));

        when(threatAssessmentUtil.evaluate(any())).thenAnswer(invocation -> {
            ThreatAssessmentArgs args = invocation.getArgument(0);
            assertFalse(args.isWhiteList());
            return assessment(ThreatAssessmentResult.ThreatLevel.MEDIUM, 68);
        });

        autoTaskService.triggerImmediateRound();

        verify(uavService, timeout(3000)).setAttackAuto(any());
        assertTrue(autoTaskService.getCurrentRoundTargetCache().containsKey("UAV-002_TRACE-002"));
    }

    @Test
    void shouldBypassWhitelistAndNoneResultInDebugScenario() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        useExecutionScenario(CountermeasureExecutionScenario.DEBUG);
        DataFusionTargetEntity target = buildTarget(
                "debug-bypass",
                LocalDateTime.now(),
                "114.432000",
                "22.707000",
                "100.0",
                "600.0",
                "4.0"
        );
        target.setWhiteListId(9);
        fusionTargetFixture.replace(List.of(target));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(ThreatAssessmentResult.builder()
                .whiteList(true)
                .threatAssessmentArea(ThreatAssessmentResult.ThreatAssessmentArea.OUTSIDE)
                .threatLevel(ThreatAssessmentResult.ThreatLevel.NONE)
                .threatScore(-1)
                .build());

        autoTaskService.triggerImmediateRound();

        verify(uavService, timeout(3000)).setAttackAuto(any());
        assertEquals(ThreatAssessmentResult.ThreatLevel.MEDIUM,
                autoTaskService.getCurrentRoundTargetCache().get("0_debug-bypass").threatLevel());
        assertFalse(autoTaskService.getCurrentRoundTargetCache().get("0_debug-bypass").whiteList());
    }

    @Test
    void shouldKeepProdScenarioFilteringWhitelistAndNoneTargets() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        useExecutionScenario(CountermeasureExecutionScenario.PROD);
        DataFusionTargetEntity target = buildTarget(
                "prod-filter",
                LocalDateTime.now(),
                "114.432500",
                "22.707500",
                "100.0",
                "600.0",
                "4.0"
        );
        target.setWhiteListId(9);
        fusionTargetFixture.replace(List.of(target));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(ThreatAssessmentResult.builder()
                .whiteList(true)
                .threatAssessmentArea(ThreatAssessmentResult.ThreatAssessmentArea.OUTSIDE)
                .threatLevel(ThreatAssessmentResult.ThreatLevel.NONE)
                .threatScore(-1)
                .build());

        autoTaskService.triggerImmediateRound();

        verify(uavService, after(800).never()).setAttackAuto(any());
        verify(talentService, after(800).never()).sendTransmitPowerCommand(any());
        assertEquals(0, autoTaskService.getCurrentRoundTargetCache().size());
    }

    @Test
    void shouldTriggerElectricAttackForMediumThreatAndCancelOnStop() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-attack",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "1200.0",
                "6.5"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.MEDIUM, 72));

        autoTaskService.triggerImmediateRound();

        verify(uavService, timeout(3000)).setAttackAuto(Mockito.argThat((AttackAutoRequest request) ->
                "E01".equals(request.getDeviceId()) && !request.getIsCancel()
        ));

        autoTaskService.stopCurrentIntervention("manual test stop");

        verify(uavService, timeout(1000)).setAttackAuto(Mockito.argThat((AttackAutoRequest request) ->
                "E01".equals(request.getDeviceId()) && request.getIsCancel()
        ));
    }

    @Test
    void shouldUseReverseDirectionFromTargetToMapCenterForDriveAngle() {
        enableAutoMode();
        useStrategy(fixedStrategyA());

        DataFusionTargetEntity target = buildTarget(
                "target-drive",
                LocalDateTime.now(),
                "114.430000000000",
                "22.705000000000",
                "120.0",
                "800.0",
                "6.5"
        );
        fusionTargetFixture.replace(List.of(target));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 95));

        autoTaskService.triggerImmediateRound();

        double expectedReverseAngle = reverseBearingDegrees(22.705000000000D, 114.430000000000D, 22.700272D, 114.427761D);
        verify(talentService, timeout(3000)).sendDriveAngleCommand(Mockito.argThat((DriveAngleRequest request) ->
                "T01".equals(request.getDeviceId()) && Math.abs(request.getAngle() - expectedReverseAngle) < 0.0001D
        ));
        verify(talentService, timeout(3000)).sendTransmitPowerCommand(Mockito.argThat((TransmitPowerRequest request) ->
                "T01".equals(request.getDeviceId()) && request.getPower() == 18
        ));
    }

    @Test
    void shouldFallbackToTargetAzimuthAndClampPowerWhenMapCenterMissing() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        configFixture.remove(CountermeasureConfigService.MAP_CENTER_POINT_KEY);
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-drive-fallback",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "50.0",
                "6.5",
                "123.456"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 88));

        autoTaskService.triggerImmediateRound();

        verify(talentService, timeout(3000)).sendDriveAngleCommand(Mockito.argThat((DriveAngleRequest request) ->
                "T01".equals(request.getDeviceId()) && Math.abs(request.getAngle() - 123.456D) < 0.0001D
        ));
        verify(talentService, timeout(3000)).sendTransmitPowerCommand(Mockito.argThat((TransmitPowerRequest request) ->
                "T01".equals(request.getDeviceId()) && request.getPower() == 0
        ));
    }

    @Test
    void shouldUseConfiguredCapturePointAndStopImmediatelyWhenSwitchingToManual() {
        enableAutoMode();
        useStrategy(fixedStrategyB());

        DataFusionTargetEntity target = buildTarget(
                "target-capture",
                LocalDateTime.now(),
                "114.420000000000",
                "22.710000000000",
                "160.0",
                "1500.0",
                "4.5"
        );
        fusionTargetFixture.replace(List.of(target));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 99));

        autoTaskService.triggerImmediateRound();

        verify(talentService, timeout(3000)).sendBootstrapPositionCommand(Mockito.argThat((PositionRequest request) ->
                "T01".equals(request.getDeviceId())
                        && Math.abs(request.getLongitude() - 114.425033D) < 0.000001D
                        && Math.abs(request.getLatitude() - 22.699680D) < 0.000001D
                        && request.getAltitude() == 20
        ));
        verify(talentService, timeout(3000)).sendCaptureCommand(Mockito.argThat((CaptureRequest request) ->
                "T01".equals(request.getDeviceId())
                        && Math.abs(request.getLongitude() - 114.425033D) < 0.000001D
                        && Math.abs(request.getLatitude() - 22.699680D) < 0.000001D
                        && request.getAltitude() == 20
        ));

        ModeUpdateRequest request = new ModeUpdateRequest();
        request.setMode(CountermeasureMode.MANUAL);
        ResponseEntity<String> response = countermeasureController.updateMode(request);
        assertEquals("模式更新成功", response.getBody());
        assertEquals(CountermeasureMode.MANUAL, countermeasureConfigService.getMode());

        verify(talentService, timeout(3000)).stopLaunch(any());
    }

    @Test
    void shouldFallbackToTargetPositionWhenCapturePointInvalid() {
        enableAutoMode();
        useStrategy(fixedStrategyB());
        configFixture.upsert(CountermeasureConfigService.CAPTURE_POINT_KEY, "自动处置捕获点", "[114.42,22.70]");
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-capture-fallback",
                LocalDateTime.now(),
                "114.420000",
                "22.710000",
                "161.0",
                "1500.0",
                "4.5"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 91));

        autoTaskService.triggerImmediateRound();

        verify(talentService, timeout(3000)).sendBootstrapPositionCommand(Mockito.argThat((PositionRequest request) ->
                "T01".equals(request.getDeviceId())
                        && Math.abs(request.getLongitude() - 114.420000D) < 0.000001D
                        && Math.abs(request.getLatitude() - 22.710000D) < 0.000001D
                        && request.getAltitude() == 161
        ));
        verify(talentService, timeout(3000)).sendCaptureCommand(Mockito.argThat((CaptureRequest request) ->
                "T01".equals(request.getDeviceId())
                        && Math.abs(request.getLongitude() - 114.420000D) < 0.000001D
                        && Math.abs(request.getLatitude() - 22.710000D) < 0.000001D
                        && request.getAltitude() == 161
        ));
    }

    @Test
    void shouldChooseMultiTargetRuleFromLiveAdaptiveStrategy() {
        enableAutoMode();
        useStrategy(adaptiveStrategyC());
        fusionTargetFixture.replace(List.of(
                buildTarget("adaptive-high", LocalDateTime.now(), "114.430000", "22.705000", "120.0", "800.0", "6.5"),
                buildTarget("adaptive-medium", LocalDateTime.now(), "114.431000", "22.706000", "90.0", "900.0", "5.5")
        ));

        when(threatAssessmentUtil.evaluate(any())).thenAnswer(invocation -> {
            ThreatAssessmentArgs args = invocation.getArgument(0);
            if (Math.abs(args.getLongitude() - 114.430000D) < 0.000001D) {
                return assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 96);
            }
            return assessment(ThreatAssessmentResult.ThreatLevel.MEDIUM, 63);
        });

        autoTaskService.triggerImmediateRound();

        verify(uavService, timeout(3000)).setAttackAuto(Mockito.argThat((AttackAutoRequest request) ->
                "E01".equals(request.getDeviceId()) && !request.getIsCancel()
        ));
        verify(talentService, after(800).never()).sendDriveAngleCommand(any());
        verify(talentService, after(800).never()).sendCaptureCommand(any());
        assertEquals(2, autoTaskService.getCurrentRoundTargetCache().size());
    }

    @Test
    void shouldRescheduleQueuedRoundWhenScanPeriodChanges() {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-reschedule",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "1200.0",
                "6.5"
        )));
        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.MEDIUM, 72));

        clearInvocations(dataFusionTargetRepository);

        configFixture.upsert(CountermeasureConfigService.SCAN_PERIOD_KEY, "自动处置扫描周期毫秒", "200");

        verify(dataFusionTargetRepository, timeout(1500)).findByTimestampBetweenOrderByTimestampDesc(any(), any());
    }

    @Test
    void shouldUpgradeToCaptureAfterConsecutiveHighDominanceRounds() throws Exception {
        enableAutoMode();
        useStrategy(adaptiveStrategyC());
        fusionTargetFixture.replace(List.of(buildTarget(
                "adaptive-dominant",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "1500.0",
                "6.5"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 97));

        autoTaskService.triggerImmediateRound();

        verify(talentService, timeout(3000)).sendDriveAngleCommand(any());

        reset(talentService);
        when(talentService.isConnected(any())).thenReturn(CONNECTED_STATUS);

        autoTaskService.triggerImmediateRound();

        verify(talentService, timeout(3000)).stopLaunch(any());
        verify(talentService, timeout(3000)).sendBootstrapPositionCommand(Mockito.argThat((PositionRequest request) ->
                "T01".equals(request.getDeviceId())
                        && Math.abs(request.getLongitude() - 114.425033D) < 0.000001D
                        && Math.abs(request.getLatitude() - 22.699680D) < 0.000001D
                        && request.getAltitude() == 20
        ));
        verify(talentService, timeout(3000)).sendCaptureCommand(Mockito.argThat((CaptureRequest request) ->
                "T01".equals(request.getDeviceId())
        ));
    }

    @Test
    void shouldSkipDeceptionWhenTalentDeviceDisconnected() throws Exception {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-disconnected",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "800.0",
                "6.5"
        )));
        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 82));
        when(talentService.isConnected(any())).thenReturn(ConnectionStatus.newBuilder().setConnected(false).build());

        autoTaskService.triggerImmediateRound();

        verify(talentService, timeout(3000)).isConnected(any());
        verify(talentService, after(800).never()).sendTransmitPowerCommand(any());
        verify(talentService, after(800).never()).sendDriveAngleCommand(any());
    }

    @Test
    void shouldSkipElectricAttackWhenElectricDeviceMissing() throws Exception {
        enableAutoMode();
        useStrategy(fixedStrategyA());
        setDevices(talentDevice("T01"));
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-no-electric",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "800.0",
                "6.5"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.MEDIUM, 70));

        autoTaskService.triggerImmediateRound();

        verify(uavService, after(800).never()).setAttackAuto(any());
    }

    @Test
    void shouldSkipCommandsWhenDeviceConfigLookupFails() throws Exception {
        enableAutoMode();
        useStrategy(fixedStrategyB());
        fusionTargetFixture.replace(List.of(buildTarget(
                "target-config-error",
                LocalDateTime.now(),
                "114.430000",
                "22.705000",
                "120.0",
                "800.0",
                "6.5"
        )));

        when(threatAssessmentUtil.evaluate(any())).thenReturn(assessment(ThreatAssessmentResult.ThreatLevel.HIGH, 90));
        when(configService.getConfig()).thenThrow(new RuntimeException("config unavailable"));

        autoTaskService.triggerImmediateRound();

        verify(talentService, after(800).never()).sendTransmitPowerCommand(any());
        verify(talentService, after(800).never()).sendCaptureCommand(any());
        verify(uavService, after(800).never()).setAttackAuto(any());
        assertEquals(1, autoTaskService.getCurrentRoundTargetCache().size());
    }

    private void enableAutoMode() {
        enableAutoMode(TEST_SCAN_PERIOD_MS);
    }

    private void enableAutoMode(long scanPeriodMs) {
        configFixture.upsert(CountermeasureConfigService.AUTO_MODE_KEY, "自动处置模式", "true");
        configFixture.upsert(CountermeasureConfigService.SCAN_PERIOD_KEY, "自动处置扫描周期毫秒", String.valueOf(scanPeriodMs));
    }

    private void useTargetDataSource(CountermeasureTargetDataSource dataSource) {
        configFixture.upsert(CountermeasureConfigService.TARGET_DATA_SOURCE_KEY, "自动处置目标来源", dataSource.name());
    }

    private void useExecutionScenario(CountermeasureExecutionScenario scenario) {
        configFixture.upsert(CountermeasureConfigService.EXECUTION_SCENARIO_KEY, "自动处置场景", scenario.name());
    }

    private void useStrategy(String strategyProfileJson) {
        configFixture.upsert(CountermeasureConfigService.STRATEGY_PROFILE_KEY, "自动处置策略配置", strategyProfileJson);
    }

    private String fixedStrategyA() {
        return """
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
                    }
                  }
                }
                """;
    }

    private String fixedStrategyB() {
        return """
                {
                  "activePreset": "B",
                  "presets": {
                    "B": {
                      "mode": "FIXED",
                      "actions": {
                        "LOW": ["UAV_ATTACK_AUTO"],
                        "MEDIUM": ["DECEPTION_DRIVE"],
                        "HIGH": ["DECEPTION_CAPTURE"]
                      },
                      "rules": {}
                    }
                  }
                }
                """;
    }

    private String adaptiveStrategyC() {
        return """
                {
                  "activePreset": "C",
                  "presets": {
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
                    }
                  }
                }
                """;
    }

    private ThreatAssessmentResult assessment(ThreatAssessmentResult.ThreatLevel threatLevel, int threatScore) {
        return ThreatAssessmentResult.builder()
                .threatLevel(threatLevel)
                .threatScore(threatScore)
                .whiteList(false)
                .build();
    }

    private void setDevices(Device... devices) throws Exception {
        DeviceConfig.Builder builder = DeviceConfig.newBuilder();
        for (Device device : devices) {
            builder.addDevices(device);
        }
        when(configService.getConfig()).thenReturn(builder.build());
    }

    private Device electricDevice(String deviceId) {
        return Device.newBuilder()
                .setDeviceId(deviceId)
                .setDeviceName("Electric-" + deviceId)
                .setDeviceType("ELECTRIC_INVESTIGATION")
                .build();
    }

    private Device talentDevice(String deviceId) {
        return Device.newBuilder()
                .setDeviceId(deviceId)
                .setDeviceName("Talent-" + deviceId)
                .setDeviceType("TALENT")
                .build();
    }

    private DataFusionTargetEntity buildTarget(
            String id,
            LocalDateTime timestamp,
            String lon,
            String lat,
            String altitude,
            String range,
            String speed
    ) {
        return buildTarget(id, timestamp, lon, lat, altitude, range, speed, "0");
    }

    private DataFusionTargetEntity buildTarget(
            String id,
            LocalDateTime timestamp,
            String lon,
            String lat,
            String altitude,
            String range,
            String speed,
            String azimuth
    ) {
        DataFusionTargetEntity entity = new DataFusionTargetEntity();
        entity.setId(id);
        entity.setTimestamp(timestamp);
        entity.setTargetBatch(0L);
        entity.setTargetId(id);
        entity.setRadarTargetId("radar-" + id);
        entity.setTdoaTargetId("tdoa-" + id);
        entity.setTargetLon(new BigDecimal(lon));
        entity.setTargetLat(new BigDecimal(lat));
        entity.setAltitude(new BigDecimal(altitude));
        entity.setRange(new BigDecimal(range));
        entity.setSpeed(new BigDecimal(speed));
        entity.setAzimuth(new BigDecimal(azimuth));
        return entity;
    }

    private double reverseBearingDegrees(double fromLat, double fromLon, double toLat, double toLon) {
        double fromLatRad = Math.toRadians(fromLat);
        double toLatRad = Math.toRadians(toLat);
        double deltaLonRad = Math.toRadians(toLon - fromLon);
        double y = Math.sin(deltaLonRad) * Math.cos(toLatRad);
        double x = Math.cos(fromLatRad) * Math.sin(toLatRad)
                - Math.sin(fromLatRad) * Math.cos(toLatRad) * Math.cos(deltaLonRad);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        double normalized = ((bearing % 360.0D) + 360.0D) % 360.0D;
        return ((normalized + 180.0D) % 360.0D + 360.0D) % 360.0D;
    }

    private DataRadarTarget buildRadarTarget(
            String id,
            LocalDateTime timestamp,
            long targetBatch,
            int targetId,
            String lon,
            String lat,
            String altitude,
            String range,
            String speed,
            String azimuth
    ) {
        DataRadarTarget entity = new DataRadarTarget();
        entity.setId(id);
        entity.setTimestamp(toDate(timestamp));
        entity.setTargetBatch(targetBatch);
        entity.setTargetId(targetId);
        entity.setTargetLon(new BigDecimal(lon));
        entity.setTargetLat(new BigDecimal(lat));
        entity.setAltitude(new BigDecimal(altitude));
        entity.setRange(new BigDecimal(range));
        entity.setSpeed(new BigDecimal(speed));
        entity.setAzimuth2(new BigDecimal(azimuth));
        entity.setIsDelete(0);
        return entity;
    }

    private DataTdoaTarget buildTdoaTarget(
            String id,
            LocalDateTime timestamp,
            String uavId,
            String traceId,
            String lon,
            String lat,
            String altitude,
            String distance,
            String speed,
            String azimuth,
            Integer whiteListId
    ) {
        DataTdoaTarget entity = new DataTdoaTarget();
        entity.setId(id);
        entity.setTimestamp(toDate(timestamp));
        entity.setTargetBatch(0L);
        entity.setUavId(uavId);
        entity.setTraceId(traceId);
        entity.setUavLon(new BigDecimal(lon));
        entity.setUavLat(new BigDecimal(lat));
        entity.setUavAlt(new BigDecimal(altitude));
        entity.setUavDistance(new BigDecimal(distance));
        entity.setVelocity(new BigDecimal(speed));
        entity.setUavAzimuth(new BigDecimal(azimuth));
        entity.setWhiteListId(whiteListId);
        return entity;
    }

    private Date toDate(LocalDateTime timestamp) {
        return Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant());
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

    static class InMemoryFusionTargetFixture {
        private final List<DataFusionTargetEntity> entities = new ArrayList<>();

        void clear() {
            entities.clear();
        }

        void replace(List<DataFusionTargetEntity> newEntities) {
            entities.clear();
            entities.addAll(newEntities);
        }

        List<DataFusionTargetEntity> findBetween(LocalDateTime start, LocalDateTime end) {
            return entities.stream()
                    .filter(entity -> !entity.getTimestamp().isBefore(start) && !entity.getTimestamp().isAfter(end))
                    .sorted((left, right) -> right.getTimestamp().compareTo(left.getTimestamp()))
                    .toList();
        }
    }

    static class InMemoryRadarTargetFixture {
        private final List<DataRadarTarget> entities = new ArrayList<>();

        void clear() {
            entities.clear();
        }

        void replace(List<DataRadarTarget> newEntities) {
            entities.clear();
            entities.addAll(newEntities);
        }

        List<DataRadarTarget> current() {
            return new ArrayList<>(entities);
        }
    }

    static class InMemoryTdoaTargetFixture {
        private final List<DataTdoaTarget> entities = new ArrayList<>();

        void clear() {
            entities.clear();
        }

        void replace(List<DataTdoaTarget> newEntities) {
            entities.clear();
            entities.addAll(newEntities);
        }

        List<DataTdoaTarget> current() {
            return new ArrayList<>(entities);
        }
    }

    @SpringBootConfiguration
    @Import({
            CountermeasureConfigService.class,
            CountermeasureRoundTargetService.class,
            CountermeasureActionExecutionService.class,
            CountermeasureAutoTaskService.class,
            CountermeasureController.class,
            OperationSseController.class,
            CountermeasureServiceImpl.class
    })
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
        InMemoryFusionTargetFixture inMemoryFusionTargetFixture() {
            return new InMemoryFusionTargetFixture();
        }

        @Bean
        InMemoryRadarTargetFixture inMemoryRadarTargetFixture() {
            return new InMemoryRadarTargetFixture();
        }

        @Bean
        InMemoryTdoaTargetFixture inMemoryTdoaTargetFixture() {
            return new InMemoryTdoaTargetFixture();
        }

        @Bean
        ConfigRepository configRepository(InMemoryConfigFixture fixture) {
            ConfigRepository repository = Mockito.mock(ConfigRepository.class);
            when(repository.findByConfigKey(any())).thenAnswer(invocation -> fixture.find(invocation.getArgument(0)));
            when(repository.save(any())).thenAnswer(invocation -> fixture.save(invocation.getArgument(0)));
            return repository;
        }

        @Bean
        DataFusionTargetRepository dataFusionTargetRepository(InMemoryFusionTargetFixture fixture) {
            DataFusionTargetRepository repository = Mockito.mock(DataFusionTargetRepository.class);
            when(repository.findByTimestampBetweenOrderByTimestampDesc(any(), any()))
                    .thenAnswer(invocation -> fixture.findBetween(invocation.getArgument(0), invocation.getArgument(1)));
            return repository;
        }

        @Bean
        DataRadarTargetMapper dataRadarTargetMapper(InMemoryRadarTargetFixture fixture) {
            DataRadarTargetMapper mapper = Mockito.mock(DataRadarTargetMapper.class);
            when(mapper.selectList(any())).thenAnswer(invocation -> fixture.current());
            return mapper;
        }

        @Bean
        DataTdoaTargetMapper dataTdoaTargetMapper(InMemoryTdoaTargetFixture fixture) {
            DataTdoaTargetMapper mapper = Mockito.mock(DataTdoaTargetMapper.class);
            when(mapper.selectList(any())).thenAnswer(invocation -> fixture.current());
            return mapper;
        }

        @Bean
        ThreatAssessmentUtil threatAssessmentUtil() {
            return Mockito.mock(ThreatAssessmentUtil.class);
        }

        @Bean
        ConfigService configService() {
            return Mockito.mock(ConfigService.class);
        }

        @Bean
        TalentService talentService() {
            return Mockito.mock(TalentService.class);
        }

        @Bean
        UavService uavService() {
            return Mockito.mock(UavService.class);
        }
    }
}
