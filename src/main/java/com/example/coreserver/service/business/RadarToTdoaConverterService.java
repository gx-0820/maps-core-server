package com.example.coreserver.service.business;

import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 雷达目标数据转TDOA工具类
 * 用于将雷达目标对象转换为TDOA目标对象
 */
@Slf4j
@Component
public class RadarToTdoaConverterService {

    /**
     * 将雷达目标列表转换为TDOA目标对象列表
     * 
     * @param radarTargets 雷达目标对象列表
     * @return TDOA目标对象列表
     */
    public List<DataTdoaTarget> convertRadarListToTdoaList(List<DataRadarTarget> radarTargets) {
        List<DataTdoaTarget> tdoaTargets = new ArrayList<>();
        
        if (radarTargets == null || radarTargets.isEmpty()) {
            log.warn("雷达目标列表为空");
            return tdoaTargets;
        }

        for (DataRadarTarget radarTarget : radarTargets) {
            DataTdoaTarget tdoaTarget = convertSingleRadarToTdoa(radarTarget);
            if (tdoaTarget != null) {
                tdoaTargets.add(tdoaTarget);
            }
        }
        
        return tdoaTargets;
    }

    /**
     * 将单个雷达目标转换为TDOA目标对象列表（返回单元素列表）
     * 
     * @param radarTarget 单个雷达目标对象
     * @return TDOA目标对象列表（包含0或1个元素）
     */
    public List<DataTdoaTarget> convertSingleRadarToTdoaList(DataRadarTarget radarTarget) {
        List<DataTdoaTarget> result = new ArrayList<>();
        
        if (radarTarget == null) {
            log.warn("雷达目标对象为空");
            return result;
        }

        DataTdoaTarget tdoaTarget = convertSingleRadarToTdoa(radarTarget);
        if (tdoaTarget != null) {
            result.add(tdoaTarget);
        }
        
        return result;
    }

    /**
     * 将单个雷达目标转换为TDOA目标对象
     * 
     * @param radarTarget 雷达目标对象
     * @return TDOA目标对象，转换失败返回null
     */
    private DataTdoaTarget convertSingleRadarToTdoa(DataRadarTarget radarTarget) {
        try {
            DataTdoaTarget tdoaTarget = new DataTdoaTarget();
            
            // 设置时间戳
            if (radarTarget.getTimestamp() != null) {
                tdoaTarget.setTimestamp(radarTarget.getTimestamp());
            } else {
                tdoaTarget.setTimestamp(new java.util.Date());
            }
            
            // 设置设备ID
            if (radarTarget.getDeviceId() != null) {
                tdoaTarget.setDeviceId(radarTarget.getDeviceId());
                tdoaTarget.setSensorId(radarTarget.getDeviceId());
            }
            
            // 设置目标批次号
            if (radarTarget.getTargetBatch() != null) {
                tdoaTarget.setTargetBatch(radarTarget.getTargetBatch());
            }
            
            // 设置无人机ID - 使用targetId作为标识
            if (radarTarget.getTargetId() != null) {
                tdoaTarget.setUavId("RADAR_" + radarTarget.getTargetId());
            }
            
            // 设置坐标信息
            if (radarTarget.getTargetLon() != null) {
                tdoaTarget.setUavLon(radarTarget.getTargetLon());
            }
            
            if (radarTarget.getTargetLat() != null) {
                tdoaTarget.setUavLat(radarTarget.getTargetLat());
            }
            
            if (radarTarget.getAltitude() != null) {
                tdoaTarget.setUavAlt(radarTarget.getAltitude());
            }
            
            // 设置速度信息
            if (radarTarget.getSpeed() != null) {
                tdoaTarget.setVelocity(radarTarget.getSpeed());
            }
            
            // 设置方位角和俯仰角
            if (radarTarget.getAzimuth2() != null) {
                tdoaTarget.setUavAzimuth(radarTarget.getAzimuth2());
            }
            
            if (radarTarget.getPitch() != null) {
                // 俯仰角可以存储到扩展字段或其他合适位置
                // 这里暂时不映射，因为TDOA实体中没有直接对应的字段
            }
            
            // 设置距离信息
            if (radarTarget.getRange() != null) {
                tdoaTarget.setUavDistance(radarTarget.getRange());
            }
            
            // 设置目标类型
            if (radarTarget.getTargetType() != null) {
                tdoaTarget.setTargetType(radarTarget.getTargetType());
            }
            
            // 设置协议类型
            if (radarTarget.getProtocolType() != null) {
                // TDOA实体中没有protocolType字段，可以记录日志或忽略
                log.debug("雷达协议类型: {}", radarTarget.getProtocolType());
            }
            
            // 设置信噪比（如果有需要可以存储到扩展字段）
            if (radarTarget.getSnr() != null) {
                log.debug("雷达信噪比: {}", radarTarget.getSnr());
            }
            
            // 设置速度分量（如果需要可以计算合速度）
            if (radarTarget.getXSpeed() != null && radarTarget.getYSpeed() != null && radarTarget.getZSpeed() != null) {
                // 可以计算三维速度的模
                double velocity = Math.sqrt(
                    Math.pow(radarTarget.getXSpeed().doubleValue(), 2) +
                    Math.pow(radarTarget.getYSpeed().doubleValue(), 2) +
                    Math.pow(radarTarget.getZSpeed().doubleValue(), 2)
                );
                // 如果speed为空，可以使用计算值
                if (radarTarget.getSpeed() == null) {
                    tdoaTarget.setVelocity(java.math.BigDecimal.valueOf(velocity));
                }
            }

            //设置无人机型号：默认写死：
            tdoaTarget.setUavModel("DRONE");

            return tdoaTarget;
            
        } catch (Exception e) {
            log.error("转换单个雷达目标失败: {}", e.getMessage(), e);
            return null;
        }
    }

}
