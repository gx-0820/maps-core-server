package com.example.coreserver.utils;

import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreatAssessmentUtilConcurrencyTest {

    private static final Path PERFORMANCE_RESULT_PATH = Path.of(
            "target",
            "benchmarks",
            "ThreatAssessmentUtilConcurrencyBenchmark.txt"
    );
    private static final double EPSILON = 1.0E-9D;

    private static final String COUNTERMEASURE_A = "[[108.7653000,34.0240000],[108.7663000,34.0240000],[108.7663000,34.0246000],[108.7653000,34.0246000],[108.7653000,34.0240000]]";
    private static final String WARNING_A = "[[108.7647000,34.0235000],[108.7669000,34.0235000],[108.7669000,34.0251000],[108.7647000,34.0251000],[108.7647000,34.0235000]]";
    private static final String DETECTION_A = "[[108.7640000,34.0229000],[108.7676000,34.0229000],[108.7676000,34.0257000],[108.7640000,34.0257000],[108.7640000,34.0229000]]";

    private static final String COUNTERMEASURE_B = "[[108.7668000,34.0248000],[108.7672000,34.0248000],[108.7672000,34.0252000],[108.7668000,34.0252000],[108.7668000,34.0248000]]";
    private static final String WARNING_B = "[[108.7644000,34.0232000],[108.7671000,34.0232000],[108.7671000,34.0253000],[108.7644000,34.0253000],[108.7644000,34.0232000]]";
    private static final String DETECTION_B = "[[108.7638000,34.0227000],[108.7677000,34.0227000],[108.7677000,34.0259000],[108.7638000,34.0259000],[108.7638000,34.0227000]]";
    private static final double COUNTERMEASURE_A_WGS_LON = 108.7608940160D;
    private static final double COUNTERMEASURE_A_WGS_LAT = 34.0257160779D;
    private static final double COUNTERMEASURE_A_FIRST_WGS_LON = 108.7605939771D;
    private static final double COUNTERMEASURE_B_FIRST_WGS_LON = 108.7620942260D;
    private static final double WARNING_OVERLAP_BASE_LON = 108.7607000000D;
    private static final double WARNING_OVERLAP_BASE_LAT = 34.0252000000D;

    @Test
    void shouldRefreshAreaConfigAfterDatabaseChanges() {
        TestableThreatAssessmentUtil util = new TestableThreatAssessmentUtil(configsA());
        util.evaluate(buildArgs("1001", LocalDateTime.now(), COUNTERMEASURE_A_WGS_LON, COUNTERMEASURE_A_WGS_LAT, 120.0D, 6.0D));
        double beforeFirstLongitude = firstCountermeasureLongitude(util);
        long beforeRefreshedAt = refreshedAtMillis(util);
        assertEquals(COUNTERMEASURE_A_FIRST_WGS_LON, beforeFirstLongitude, EPSILON);

        util.replaceConfigs(configsB());
        util.refreshAreaConfigPeriodically();

        double afterFirstLongitude = firstCountermeasureLongitude(util);
        long afterRefreshedAt = refreshedAtMillis(util);
        assertEquals(COUNTERMEASURE_B_FIRST_WGS_LON, afterFirstLongitude, EPSILON);
        assertTrue(afterRefreshedAt >= beforeRefreshedAt);
    }

    @Test
    void shouldSeparateDetectionAreaFromThreatLevel() {
        TestableThreatAssessmentUtil util = new TestableThreatAssessmentUtil(configsA());
        ThreatAssessmentArgs args = buildArgs("3001", LocalDateTime.now(), COUNTERMEASURE_A_WGS_LON, COUNTERMEASURE_A_WGS_LAT, 120.0D, 4.0D);
        util.evaluate(args);

        ThreatAssessmentResult.ThreatLevel threatLevel = determineThreatLevel(
                util,
                args,
                ThreatAssessmentResult.ThreatAssessmentArea.DETECTION,
                false,
                false
        );
        assertEquals(ThreatAssessmentResult.ThreatLevel.NONE, threatLevel);

        Integer threatScore = calculateThreatScore(
                util,
                threatLevel,
                args,
                false,
                false
        );
        assertEquals(ThreatAssessmentResult.ThreatLevel.NONE.getValue(), threatScore);
    }

    @Test
    void shouldRemainThreadSafeDuringConcurrentEvaluateAndRefresh() throws Exception {
        TestableThreatAssessmentUtil util = new TestableThreatAssessmentUtil(configsA());
        util.evaluate(buildArgs("1", LocalDateTime.now(), COUNTERMEASURE_A_WGS_LON, COUNTERMEASURE_A_WGS_LAT, 120.0D, 8.0D));

        int evaluateWorkers = 12;
        int iterationsPerWorker = 120;
        ExecutorService executor = Executors.newFixedThreadPool(evaluateWorkers + 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int worker = 0; worker < evaluateWorkers; worker++) {
            final int workerNo = worker;
            futures.add(executor.submit(() -> {
                await(startLatch);
                try {
                    for (int index = 0; index < iterationsPerWorker; index++) {
                        LocalDateTime timestamp = LocalDateTime.now().plusNanos(workerNo * 1_000_000L + index);
                        ThreatAssessmentArgs args = buildArgs(
                                String.valueOf(workerNo % 8 + 1),
                                timestamp,
                                WARNING_OVERLAP_BASE_LON + workerNo * 0.00003D + index * 0.000001D,
                                WARNING_OVERLAP_BASE_LAT + workerNo * 0.00002D + index * 0.000001D,
                                100.0D + workerNo,
                                5.0D + (index % 12)
                        );
                        ThreatAssessmentResult result = util.evaluate(args);
                        assertNotNull(result);
                        if (result.getThreatScore() != null) {
                            assertTrue(result.getThreatScore() >= 1 && result.getThreatScore() <= 100);
                        }
                    }
                } catch (Throwable throwable) {
                    failures.add(throwable);
                }
            }));
        }

        futures.add(executor.submit(() -> {
            await(startLatch);
            try {
                for (int index = 0; index < 80; index++) {
                    util.replaceConfigs(index % 2 == 0 ? configsA() : configsB());
                    util.refreshAreaConfigPeriodically();
                }
            } catch (Throwable throwable) {
                failures.add(throwable);
            }
        }));

        startLatch.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), () -> "Concurrent test failures: " + failures);
    }

    @Test
    void shouldMeasureAverageEvaluateLatencyWhenTrackingTenTargetsConcurrently() throws Exception {
        TestableThreatAssessmentUtil util = new TestableThreatAssessmentUtil(configsA());
        int targetCount = 10;
        int warmupIterationsPerTarget = 40;
        int measuredIterationsPerTarget = 300;

        for (int targetIndex = 0; targetIndex < targetCount; targetIndex++) {
            for (int iteration = 0; iteration < warmupIterationsPerTarget; iteration++) {
                ThreatAssessmentArgs warmupArgs = buildBenchmarkArgs(targetIndex, iteration);
                ThreatAssessmentResult result = util.evaluate(warmupArgs);
                assertNotNull(result);
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(targetCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        LongAdder totalDurationNanos = new LongAdder();
        LongAdder totalInvocationCount = new LongAdder();
        List<Future<?>> futures = new ArrayList<>();

        for (int targetIndex = 0; targetIndex < targetCount; targetIndex++) {
            final int workerNo = targetIndex;
            futures.add(executor.submit(() -> {
                await(startLatch);
                try {
                    for (int iteration = 0; iteration < measuredIterationsPerTarget; iteration++) {
                        ThreatAssessmentArgs args = buildBenchmarkArgs(workerNo, warmupIterationsPerTarget + iteration);
                        long start = System.nanoTime();
                        ThreatAssessmentResult result = util.evaluate(args);
                        long duration = System.nanoTime() - start;
                        assertNotNull(result);
                        totalDurationNanos.add(duration);
                        totalInvocationCount.increment();
                    }
                } catch (Throwable throwable) {
                    failures.add(throwable);
                }
            }));
        }

        startLatch.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), () -> "Benchmark failures: " + failures);

        long invocationCount = totalInvocationCount.sum();
        assertEquals((long) targetCount * measuredIterationsPerTarget, invocationCount);

        double averageNanos = (double) totalDurationNanos.sum() / invocationCount;
        double averageMicros = averageNanos / 1_000.0D;
        double averageMillis = averageNanos / 1_000_000.0D;
        writePerformanceResult(
                targetCount,
                warmupIterationsPerTarget,
                measuredIterationsPerTarget,
                invocationCount,
                averageNanos,
                averageMicros,
                averageMillis
        );
        assertTrue(averageNanos > 0.0D);
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

    private static ThreatAssessmentArgs buildBenchmarkArgs(int targetIndex, int iteration) {
        double longitude = WARNING_OVERLAP_BASE_LON + targetIndex * 0.00008D + iteration * 0.000001D;
        double latitude = WARNING_OVERLAP_BASE_LAT + targetIndex * 0.00005D + iteration * 0.000001D;
        double altitude = 100.0D + targetIndex * 2.0D;
        double speed = 6.0D + (iteration % 8);
        return buildArgs(
                String.valueOf(targetIndex + 1),
                LocalDateTime.now().plusNanos(targetIndex * 1_000_000L + iteration),
                longitude,
                latitude,
                altitude,
                speed
        );
    }

    private static void writePerformanceResult(
            int targetCount,
            int warmupIterationsPerTarget,
            int measuredIterationsPerTarget,
            long invocationCount,
            double averageNanos,
            double averageMicros,
            double averageMillis
    ) {
        try {
            Files.createDirectories(PERFORMANCE_RESULT_PATH.getParent());
            String content = String.format(
                    Locale.ROOT,
                    """
                    scenario=10-target-concurrent-tracking
                    targetCount=%d
                    warmupIterationsPerTarget=%d
                    measuredIterationsPerTarget=%d
                    totalInvocations=%d
                    averageEvaluateNanos=%.2f
                    averageEvaluateMicros=%.2f
                    averageEvaluateMillis=%.6f
                    """,
                    targetCount,
                    warmupIterationsPerTarget,
                    measuredIterationsPerTarget,
                    invocationCount,
                    averageNanos,
                    averageMicros,
                    averageMillis
            );
            Files.writeString(
                    PERFORMANCE_RESULT_PATH,
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (Exception e) {
            throw new AssertionError("Failed to write performance result", e);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Latch waiting interrupted", e);
        }
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

    private static long refreshedAtMillis(ThreatAssessmentUtil util) {
        try {
            Field snapshotField = ThreatAssessmentUtil.class.getDeclaredField("areaConfigSnapshot");
            snapshotField.setAccessible(true);
            Object snapshot = snapshotField.get(util);
            Method refreshedAtMethod = snapshot.getClass().getDeclaredMethod("refreshedAtMillis");
            refreshedAtMethod.setAccessible(true);
            return (long) refreshedAtMethod.invoke(snapshot);
        } catch (Exception e) {
            throw new AssertionError("Failed to inspect refresh timestamp", e);
        }
    }

    private static ThreatAssessmentResult.ThreatLevel determineThreatLevel(
            ThreatAssessmentUtil util,
            ThreatAssessmentArgs args,
            ThreatAssessmentResult.ThreatAssessmentArea currentArea,
            boolean movingTowardCountermeasure,
            boolean swarmTarget
    ) {
        try {
            Method method = ThreatAssessmentUtil.class.getDeclaredMethod(
                    "determineThreatLevel",
                    ThreatAssessmentArgs.class,
                    ThreatAssessmentResult.ThreatAssessmentArea.class,
                    boolean.class,
                    boolean.class
            );
            method.setAccessible(true);
            return (ThreatAssessmentResult.ThreatLevel) method.invoke(
                    util,
                    args,
                    currentArea,
                    movingTowardCountermeasure,
                    swarmTarget
            );
        } catch (Exception e) {
            throw new AssertionError("Failed to invoke determineThreatLevel", e);
        }
    }

    private static Integer calculateThreatScore(
            ThreatAssessmentUtil util,
            ThreatAssessmentResult.ThreatLevel threatLevel,
            ThreatAssessmentArgs args,
            boolean movingTowardCountermeasure,
            boolean swarmTarget
    ) {
        try {
            Field snapshotField = ThreatAssessmentUtil.class.getDeclaredField("areaConfigSnapshot");
            snapshotField.setAccessible(true);
            Object snapshot = snapshotField.get(util);

            Method method = ThreatAssessmentUtil.class.getDeclaredMethod(
                    "calculateThreatScore",
                    ThreatAssessmentResult.ThreatLevel.class,
                    ThreatAssessmentArgs.class,
                    boolean.class,
                    boolean.class,
                    snapshot.getClass()
            );
            method.setAccessible(true);
            return (Integer) method.invoke(
                    util,
                    threatLevel,
                    args,
                    movingTowardCountermeasure,
                    swarmTarget,
                    snapshot
            );
        } catch (Exception e) {
            throw new AssertionError("Failed to invoke calculateThreatScore", e);
        }
    }

    private static final class TestableThreatAssessmentUtil extends ThreatAssessmentUtil {
        private final AtomicReference<List<Config>> configRef;

        private TestableThreatAssessmentUtil(List<Config> initialConfigs) {
            super(null, new ObjectMapper());
            this.configRef = new AtomicReference<>(initialConfigs);
        }

        private void replaceConfigs(List<Config> configs) {
            this.configRef.set(configs);
        }

        @Override
        protected List<Config> loadThreatAssessmentConfigs() {
            return configRef.get();
        }
    }
}
