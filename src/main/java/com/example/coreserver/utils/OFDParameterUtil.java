package com.example.coreserver.utils;

import com.example.coreserver.service.device.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * OFD（光电设备）参数工具类
 * 提供便捷的OFD参数获取方法，确保所有使用OFD参数的地方都从数据库获取
 * 
 * @author: zhanghenan
 */
@Component
@Slf4j
public class OFDParameterUtil {

    @Autowired
    private ConfigService configService;

    /**
     * 获取目标距离偏差
     * 
     * @return 目标距离偏差值（如果获取失败则返回0）
     */
    public Double getRangeDeviation() {
        try {
            String value = configService.getConfigValue("sys.OFD.rangeDDeviation");
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (Exception e) {
            log.error("Failed to get range deviation from config", e);
            return 0.0;
        }
    }

    /**
     * 获取目标方位角偏差
     * 
     * @return 目标方位角偏差值（如果获取失败则返回0）
     */
    public Double getAzimuthDeviation() {
        try {
            String value = configService.getConfigValue("sys.OFD.azimuthDeviation");
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (Exception e) {
            log.error("Failed to get azimuth deviation from config", e);
            return 0.0;
        }
    }

    /**
     * 获取目标俯仰角偏差
     * 
     * @return 目标俯仰角偏差值（如果获取失败则返回0）
     */
    public Double getElevationDeviation() {
        try {
            String value = configService.getConfigValue("sys.OFD.elevationDeviation");
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (Exception e) {
            log.error("Failed to get elevation deviation from config", e);
            return 0.0;
        }
    }

    /**
     * 获取目标距离偏差（字符串形式）
     * 
     * @return 目标距离偏差字符串值
     */
    public String getRangeDeviationStr() {
        try {
            String value = configService.getConfigValue("sys.OFD.rangeDDeviation");
            return value != null ? value : "0";
        } catch (Exception e) {
            log.error("Failed to get range deviation string from config", e);
            return "0";
        }
    }

    /**
     * 获取目标方位角偏差（字符串形式）
     * 
     * @return 目标方位角偏差字符串值
     */
    public String getAzimuthDeviationStr() {
        try {
            String value = configService.getConfigValue("sys.OFD.azimuthDeviation");
            return value != null ? value : "0";
        } catch (Exception e) {
            log.error("Failed to get azimuth deviation string from config", e);
            return "0";
        }
    }

    /**
     * 获取目标俯仰角偏差（字符串形式）
     * 
     * @return 目标俯仰角偏差字符串值
     */
    public String getElevationDeviationStr() {
        try {
            String value = configService.getConfigValue("sys.OFD.elevationDeviation");
            return value != null ? value : "0";
        } catch (Exception e) {
            log.error("Failed to get elevation deviation string from config", e);
            return "0";
        }
    }

    /**
     * 校准目标距离值
     * 
     * @param originalDistance 原始距离值
     * @return 校准后的距离值
     */
    public Double calibrateDistance(Double originalDistance) {
        try {
            if (originalDistance == null) {
                return 0.0;
            }
            Double deviation = getRangeDeviation();
            return originalDistance - deviation;
        } catch (Exception e) {
            log.error("Failed to calibrate distance", e);
            return originalDistance;
        }
    }

    /**
     * 校准目标方位角值
     * 
     * @param originalAzimuth 原始方位角值
     * @return 校准后的方位角值
     */
    public Double calibrateAzimuth(Double originalAzimuth) {
        try {
            if (originalAzimuth == null) {
                return 0.0;
            }
            Double deviation = getAzimuthDeviation();
            return originalAzimuth - deviation;
        } catch (Exception e) {
            log.error("Failed to calibrate azimuth", e);
            return originalAzimuth;
        }
    }

    /**
     * 校准目标俯仰角值
     * 
     * @param originalElevation 原始俯仰角值
     * @return 校准后的俯仰角值
     */
    public Double calibrateElevation(Double originalElevation) {
        try {
            if (originalElevation == null) {
                return 0.0;
            }
            Double deviation = getElevationDeviation();
            return originalElevation - deviation;
        } catch (Exception e) {
            log.error("Failed to calibrate elevation", e);
            return originalElevation;
        }
    }
}
