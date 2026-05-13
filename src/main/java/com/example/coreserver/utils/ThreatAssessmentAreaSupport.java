package com.example.coreserver.utils;

import com.example.coreserver.entity.Config;
import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;
import com.example.coreserver.entity.threat.ThreatAssessmentResult.ThreatAssessmentArea;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 威胁评估区域配置与几何计算支撑类。
 */
@RequiredArgsConstructor
@Slf4j
class ThreatAssessmentAreaSupport {

    static final String COUNTERMEASURE_CONFIG_KEY = "sys.zone.countermeasure";
    static final String WARNING_CONFIG_KEY = "sys.zone.warning";
    static final String DETECTION_CONFIG_KEY = "sys.zone.detection";

    private static final double EARTH_RADIUS_METERS = 6_371_000.0D;
    private static final double COORDINATE_EPSILON = 1.0E-9D;
    private static final double MAX_SCORING_SPEED_METERS_PER_SECOND = 20.0D;
    private static final double CHINA_GEO_A = 6378245.0D;
    private static final double CHINA_GEO_EE = 0.00669342162296594323D;
    private static final double CHINA_LON_MIN = 72.004D;
    private static final double CHINA_LON_MAX = 137.8347D;
    private static final double CHINA_LAT_MIN = 0.8293D;
    private static final double CHINA_LAT_MAX = 55.8271D;

    private final ObjectMapper objectMapper;

    AreaConfigSnapshot buildAreaConfigSnapshot(List<Config> configList) {
        Map<String, String> configValueMap = new LinkedHashMap<>();
        for (Config config : configList) {
            configValueMap.put(config.getConfigKey(), config.getConfigValue());
        }

        List<MutablePair<Double, Double>> countermeasureCoordinates = parseCoordinateConfig(
                COUNTERMEASURE_CONFIG_KEY,
                configValueMap.get(COUNTERMEASURE_CONFIG_KEY)
        );
        List<MutablePair<Double, Double>> warningCoordinates = parseCoordinateConfig(
                WARNING_CONFIG_KEY,
                configValueMap.get(WARNING_CONFIG_KEY)
        );
        List<MutablePair<Double, Double>> detectionCoordinates = parseCoordinateConfig(
                DETECTION_CONFIG_KEY,
                configValueMap.get(DETECTION_CONFIG_KEY)
        );

        MutablePair<Double, Double> centroid = calculatePolygonCentroid(countermeasureCoordinates);
        double referenceDistance = calculateMaxThreatReferenceDistance(
                centroid,
                countermeasureCoordinates,
                warningCoordinates,
                detectionCoordinates
        );
        log.info("威胁评估区域快照构建完成: countermeasurePoints={}, warningPoints={}, detectionPoints={}, centroid={}, referenceDistanceMeters={}",
                countermeasureCoordinates.size(), warningCoordinates.size(), detectionCoordinates.size(), centroid, referenceDistance);
        return new AreaConfigSnapshot(
                countermeasureCoordinates,
                warningCoordinates,
                detectionCoordinates,
                centroid,
                referenceDistance,
                System.currentTimeMillis()
        );
    }

    ThreatAssessmentArea resolveArea(ThreatAssessmentArgs args, AreaConfigSnapshot areaSnapshot) {
        if (isInsidePolygon(args, areaSnapshot.countermeasureCoordinates())) {
            return ThreatAssessmentArea.COUNTERMEASURE;
        }
        if (isInsidePolygon(args, areaSnapshot.warningCoordinates())) {
            return ThreatAssessmentArea.WARNING;
        }
        if (isInsidePolygon(args, areaSnapshot.detectionCoordinates())) {
            return ThreatAssessmentArea.DETECTION;
        }
        return ThreatAssessmentArea.OUTSIDE;
    }

    double calculateDistanceFactor(ThreatAssessmentArgs args, AreaConfigSnapshot areaSnapshot) {
        if (areaSnapshot.countermeasureCentroid() == null) {
            return 0.0D;
        }
        double distance = calculateDistanceToCountermeasureCenter(args, areaSnapshot);
        return clamp01(1.0D - distance / areaSnapshot.maxThreatReferenceDistance());
    }

    double calculateDistanceToCountermeasureCenter(ThreatAssessmentArgs args, AreaConfigSnapshot areaSnapshot) {
        if (areaSnapshot.countermeasureCentroid() == null) {
            return Double.MAX_VALUE;
        }
        return calculateHorizontalDistanceMeters(
                args.getLatitude(),
                args.getLongitude(),
                areaSnapshot.countermeasureCentroid().right,
                areaSnapshot.countermeasureCentroid().left
        );
    }

    double calculateDistance3D(ThreatAssessmentArgs first, ThreatAssessmentArgs second) {
        double horizontalDistance = calculateHorizontalDistanceMeters(
                first.getLatitude(),
                first.getLongitude(),
                second.getLatitude(),
                second.getLongitude()
        );
        double altitudeDiff = second.getAltitude() - first.getAltitude();
        return Math.sqrt(horizontalDistance * horizontalDistance + altitudeDiff * altitudeDiff);
    }

    double[] calculatePlanarVectorMeters(double lon1, double lat1, double lon2, double lat2) {
        double averageLatRad = Math.toRadians((lat1 + lat2) / 2.0D);
        double east = Math.toRadians(lon2 - lon1) * EARTH_RADIUS_METERS * Math.cos(averageLatRad);
        double north = Math.toRadians(lat2 - lat1) * EARTH_RADIUS_METERS;
        return new double[]{east, north};
    }

    double calculateAngleBetweenVectorsDegrees(
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        double magnitude1 = Math.hypot(x1, y1);
        double magnitude2 = Math.hypot(x2, y2);
        if (magnitude1 < COORDINATE_EPSILON || magnitude2 < COORDINATE_EPSILON) {
            return 180.0D;
        }
        double cosine = (x1 * x2 + y1 * y2) / (magnitude1 * magnitude2);
        return Math.toDegrees(Math.acos(Math.clamp(cosine, -1.0D, 1.0D)));
    }

    double normalizeSpeed(double speed) {
        return clamp01(Math.max(speed, 0.0D) / MAX_SCORING_SPEED_METERS_PER_SECOND);
    }

    int clampScore(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    String summarizeConfigValue(String configValue) {
        if (configValue == null) {
            return "null";
        }
        String normalized = configValue.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 117) + "...";
    }

    /**
     * config 表中的三区坐标按 GCJ-02 存储，威胁评估统一转换成 WGS84 后再做几何计算。
     *
     * @param configName 配置键，用于日志定位当前处理的是哪个区域配置
     * @param configValue config 表中的原始 JSON 坐标数组字符串，坐标点格式为 [lon, lat]
     * @return 转换到 WGS84 后的不可变多边形坐标列表；当配置缺失、格式错误或有效点不足时返回空列表
     */
    private List<MutablePair<Double, Double>> parseCoordinateConfig(String configName, String configValue) {
        if (configValue == null || configValue.isBlank()) {
            log.warn("威胁评估区域配置为空: key={}", configName);
            return Collections.emptyList();
        }
        try {
            JsonNode root = objectMapper.readTree(configValue);
            if (!root.isArray()) {
                log.warn("威胁评估区域配置格式错误，期望JSON数组: key={}, raw={}", configName, summarizeConfigValue(configValue));
                return Collections.emptyList();
            }
            List<MutablePair<Double, Double>> coordinates = new ArrayList<>();
            for (JsonNode coordinateNode : root) {
                if (!coordinateNode.isArray() || coordinateNode.size() < 2) {
                    continue;
                }
                coordinates.add(MutablePair.of(
                        coordinateNode.get(0).asDouble(),
                        coordinateNode.get(1).asDouble()
                ));
            }
            if (coordinates.size() < 3) {
                log.warn("威胁评估区域配置有效点位不足3个: key={}, raw={}", configName, summarizeConfigValue(configValue));
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(convertPolygonFromGcj02ToWgs84(configName, coordinates));
        } catch (Exception e) {
            log.error("威胁评估区域配置解析失败: key={}, error={}, raw={}", configName, e.getMessage(), summarizeConfigValue(configValue), e);
            return Collections.emptyList();
        }
    }

    /**
     * 将单个区域配置中的整组多边形坐标从 GCJ-02 转成 WGS84。
     *
     * @param configName 当前区域配置键，仅用于日志标识
     * @param coordinates 从配置中解析出的原始 GCJ-02 坐标点列表，元素格式为 [longitude, latitude]
     * @return 与输入顺序一致的 WGS84 坐标点列表
     */
    private List<MutablePair<Double, Double>> convertPolygonFromGcj02ToWgs84(
            String configName,
            List<MutablePair<Double, Double>> coordinates
    ) {
        List<MutablePair<Double, Double>> convertedCoordinates = new ArrayList<>(coordinates.size());
        for (MutablePair<Double, Double> coordinate : coordinates) {
            convertedCoordinates.add(convertGcj02ToWgs84(coordinate.left, coordinate.right));
        }
        MutablePair<Double, Double> firstRaw = coordinates.getFirst();
        MutablePair<Double, Double> firstConverted = convertedCoordinates.getFirst();
        log.info(
                "威胁评估区域坐标系转换完成: key={}, source=GCJ-02, target=WGS84, pointCount={}, firstRaw=[{}, {}], firstConverted=[{}, {}]",
                configName,
                convertedCoordinates.size(),
                firstRaw.left,
                firstRaw.right,
                firstConverted.left,
                firstConverted.right
        );
        return convertedCoordinates;
    }

    /**
     * 将单个 GCJ-02 坐标点反算为 WGS84 坐标点。
     *
     * @param longitude GCJ-02 经度
     * @param latitude GCJ-02 纬度
     * @return 转换后的 WGS84 坐标；若点位不在中国大陆偏移范围内，则直接返回原值
     */
    private MutablePair<Double, Double> convertGcj02ToWgs84(double longitude, double latitude) {
        if (isOutsideChina(longitude, latitude)) {
            return MutablePair.of(longitude, latitude);
        }
        MutablePair<Double, Double> delta = calculateGcj02Offset(longitude, latitude);
        return MutablePair.of(
                longitude - delta.left,
                latitude - delta.right
        );
    }

    /**
     * 判断坐标点是否处于 GCJ-02 偏移算法的适用范围之外。
     *
     * @param longitude 待判断的经度
     * @param latitude 待判断的纬度
     * @return true 表示不在中国大陆偏移范围内，应跳过坐标系转换；false 表示需要继续参与转换
     */
    private boolean isOutsideChina(double longitude, double latitude) {
        return longitude < CHINA_LON_MIN
                || longitude > CHINA_LON_MAX
                || latitude < CHINA_LAT_MIN
                || latitude > CHINA_LAT_MAX;
    }

    /**
     * 计算指定 GCJ-02 点相对 WGS84 的经纬度偏移量。
     *
     * @param longitude GCJ-02 经度
     * @param latitude GCJ-02 纬度
     * @return 偏移量对，left 为经度偏移，right 为纬度偏移
     */
    private MutablePair<Double, Double> calculateGcj02Offset(double longitude, double latitude) {
        double transformedLatitude = transformLatitude(longitude - 105.0D, latitude - 35.0D);
        double transformedLongitude = transformLongitude(longitude - 105.0D, latitude - 35.0D);
        double latitudeRad = Math.toRadians(latitude);
        double magic = Math.sin(latitudeRad);
        magic = 1.0D - CHINA_GEO_EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        // 将投影平面上的扰动项换算回真实经纬度角度偏移。
        double latitudeOffset = (transformedLatitude * 180.0D)
                / ((CHINA_GEO_A * (1.0D - CHINA_GEO_EE)) / (magic * sqrtMagic) * Math.PI);
        double longitudeOffset = (transformedLongitude * 180.0D)
                / (CHINA_GEO_A / sqrtMagic * Math.cos(latitudeRad) * Math.PI);
        return MutablePair.of(longitudeOffset, latitudeOffset);
    }

    /**
     * 计算 GCJ-02 公式中的纬度扰动项。
     *
     * @param longitude 以 105 为原点平移后的经度差值
     * @param latitude 以 35 为原点平移后的纬度差值
     * @return 用于后续偏移量换算的纬度扰动值
     */
    private double transformLatitude(double longitude, double latitude) {
        double result = -100.0D + 2.0D * longitude + 3.0D * latitude + 0.2D * latitude * latitude
                + 0.1D * longitude * latitude + 0.2D * Math.sqrt(Math.abs(longitude));
        result += (20.0D * Math.sin(6.0D * longitude * Math.PI) + 20.0D * Math.sin(2.0D * longitude * Math.PI)) * 2.0D / 3.0D;
        result += (20.0D * Math.sin(latitude * Math.PI) + 40.0D * Math.sin(latitude / 3.0D * Math.PI)) * 2.0D / 3.0D;
        result += (160.0D * Math.sin(latitude / 12.0D * Math.PI) + 320.0D * Math.sin(latitude * Math.PI / 30.0D)) * 2.0D / 3.0D;
        return result;
    }

    /**
     * 计算 GCJ-02 公式中的经度扰动项。
     *
     * @param longitude 以 105 为原点平移后的经度差值
     * @param latitude 以 35 为原点平移后的纬度差值
     * @return 用于后续偏移量换算的经度扰动值
     */
    private double transformLongitude(double longitude, double latitude) {
        double result = 300.0D + longitude + 2.0D * latitude + 0.1D * longitude * longitude
                + 0.1D * longitude * latitude + 0.1D * Math.sqrt(Math.abs(longitude));
        result += (20.0D * Math.sin(6.0D * longitude * Math.PI) + 20.0D * Math.sin(2.0D * longitude * Math.PI)) * 2.0D / 3.0D;
        result += (20.0D * Math.sin(longitude * Math.PI) + 40.0D * Math.sin(longitude / 3.0D * Math.PI)) * 2.0D / 3.0D;
        result += (150.0D * Math.sin(longitude / 12.0D * Math.PI) + 300.0D * Math.sin(longitude / 30.0D * Math.PI)) * 2.0D / 3.0D;
        return result;
    }

    private boolean isInsidePolygon(ThreatAssessmentArgs args, List<MutablePair<Double, Double>> polygon) {
        return isInsidePolygon(args.getLongitude(), args.getLatitude(), polygon);
    }

    private boolean isInsidePolygon(double longitude, double latitude, List<MutablePair<Double, Double>> polygon) {
        List<MutablePair<Double, Double>> normalizedPolygon = normalizePolygon(polygon);
        if (normalizedPolygon.size() < 3) {
            return false;
        }
        boolean inside = false;
        for (int current = 0, previous = normalizedPolygon.size() - 1; current < normalizedPolygon.size(); previous = current++) {
            double currentLon = normalizedPolygon.get(current).left;
            double currentLat = normalizedPolygon.get(current).right;
            double previousLon = normalizedPolygon.get(previous).left;
            double previousLat = normalizedPolygon.get(previous).right;

            if (isPointOnSegment(longitude, latitude, currentLon, currentLat, previousLon, previousLat)) {
                return true;
            }

            boolean intersect = ((currentLat > latitude) != (previousLat > latitude))
                    && (longitude < (previousLon - currentLon) * (latitude - currentLat) / (previousLat - currentLat) + currentLon);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    private boolean isPointOnSegment(
            double pointLon,
            double pointLat,
            double startLon,
            double startLat,
            double endLon,
            double endLat
    ) {
        double deltaLon = endLon - startLon;
        double deltaLat = endLat - startLat;
        double squaredLength = deltaLon * deltaLon + deltaLat * deltaLat;
        if (squaredLength <= COORDINATE_EPSILON * COORDINATE_EPSILON) {
            return Math.abs(pointLon - startLon) <= COORDINATE_EPSILON
                    && Math.abs(pointLat - startLat) <= COORDINATE_EPSILON;
        }

        double cross = (pointLat - startLat) * (endLon - startLon) - (pointLon - startLon) * (endLat - startLat);
        if (Math.abs(cross) > COORDINATE_EPSILON) {
            return false;
        }
        double dot = (pointLon - startLon) * (endLon - startLon) + (pointLat - startLat) * (endLat - startLat);
        if (dot < 0) {
            return false;
        }
        return dot <= squaredLength;
    }

    private MutablePair<Double, Double> calculatePolygonCentroid(List<MutablePair<Double, Double>> polygon) {
        List<MutablePair<Double, Double>> normalizedPolygon = normalizePolygon(polygon);
        if (normalizedPolygon.size() < 3) {
            return null;
        }

        double signedArea = 0.0D;
        double centroidX = 0.0D;
        double centroidY = 0.0D;

        for (int index = 0; index < normalizedPolygon.size(); index++) {
            MutablePair<Double, Double> current = normalizedPolygon.get(index);
            MutablePair<Double, Double> next = normalizedPolygon.get((index + 1) % normalizedPolygon.size());
            double factor = current.left * next.right - next.left * current.right;
            signedArea += factor;
            centroidX += (current.left + next.left) * factor;
            centroidY += (current.right + next.right) * factor;
        }

        if (Math.abs(signedArea) < COORDINATE_EPSILON) {
            double avgLongitude = 0.0D;
            double avgLatitude = 0.0D;
            for (MutablePair<Double, Double> coordinate : normalizedPolygon) {
                avgLongitude += coordinate.left;
                avgLatitude += coordinate.right;
            }
            return MutablePair.of(
                    avgLongitude / normalizedPolygon.size(),
                    avgLatitude / normalizedPolygon.size()
            );
        }

        double area = signedArea * 0.5D;
        return MutablePair.of(
                centroidX / (6.0D * area),
                centroidY / (6.0D * area)
        );
    }

    private List<MutablePair<Double, Double>> normalizePolygon(List<MutablePair<Double, Double>> polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return Collections.emptyList();
        }
        List<MutablePair<Double, Double>> normalizedPolygon = new ArrayList<>(polygon);
        if (normalizedPolygon.size() > 1) {
            MutablePair<Double, Double> first = normalizedPolygon.getFirst();
            MutablePair<Double, Double> last = normalizedPolygon.getLast();
            if (Math.abs(first.left - last.left) < COORDINATE_EPSILON
                    && Math.abs(first.right - last.right) < COORDINATE_EPSILON) {
                normalizedPolygon.removeLast();
            }
        }
        return normalizedPolygon;
    }

    private double calculateMaxThreatReferenceDistance(
            MutablePair<Double, Double> centroid,
            List<MutablePair<Double, Double>> countermeasureCoordinates,
            List<MutablePair<Double, Double>> warningCoordinates,
            List<MutablePair<Double, Double>> detectionCoordinates
    ) {
        if (centroid == null) {
            return 1.0D;
        }
        List<MutablePair<Double, Double>> referencePolygon = !detectionCoordinates.isEmpty()
                ? detectionCoordinates
                : (!warningCoordinates.isEmpty() ? warningCoordinates : countermeasureCoordinates);
        double maxDistance = 0.0D;
        for (MutablePair<Double, Double> coordinate : referencePolygon) {
            maxDistance = Math.max(
                    maxDistance,
                    calculateHorizontalDistanceMeters(
                            centroid.right,
                            centroid.left,
                            coordinate.right,
                            coordinate.left
                    )
            );
        }
        return Math.max(maxDistance, 1.0D);
    }

    private double calculateHorizontalDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        if (!isFinite(lat1) || !isFinite(lon1) || !isFinite(lat2) || !isFinite(lon2)) {
            return Double.MAX_VALUE;
        }
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(deltaLat / 2.0D) * Math.sin(deltaLat / 2.0D)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2.0D) * Math.sin(deltaLon / 2.0D);
        double c = 2.0D * Math.atan2(Math.sqrt(a), Math.sqrt(1.0D - a));
        return EARTH_RADIUS_METERS * c;
    }

    private double clamp01(double value) {
        return Math.clamp(value, 0.0D, 1.0D);
    }

    record AreaConfigSnapshot(
            List<MutablePair<Double, Double>> countermeasureCoordinates,
            List<MutablePair<Double, Double>> warningCoordinates,
            List<MutablePair<Double, Double>> detectionCoordinates,
            MutablePair<Double, Double> countermeasureCentroid,
            double maxThreatReferenceDistance,
            long refreshedAtMillis
    ) {
        static AreaConfigSnapshot empty() {
            return new AreaConfigSnapshot(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null,
                    1.0D,
                    0L
            );
        }
    }
}
