package com.example.coreserver.handler;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.config.MqttConfig;
import com.example.coreserver.entity.data.RadarTarget;
import com.example.coreserver.utils.SilasUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RadarDataHandler {

    @Autowired
    private SilasUtils silasUtils;

    @Autowired
    private MqttConfig mqttConfig;

    @Async
    public void handleRadarData2Silas(JSONObject radarData) {
        // 处理雷达数据的逻辑
        if (mqttConfig.getStatus() == 1) {
            try {
                JSONArray targets = radarData.getJSONArray("targets");
                List<RadarTarget> list = targets.toJavaList(RadarTarget.class);
                for (RadarTarget target : list) {
                    // 处理每个目标
                    silasUtils.sendRadarTargetToSilas(target);
                }
            } catch (Exception e) {
                log.error("Failed to send silas message" + e.getMessage());
            }
        }
    }

}
