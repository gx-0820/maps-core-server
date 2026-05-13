package com.example.coreserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;

import java.util.List;

/**
* @author 70411
* @description 针对表【data_radar_target(雷达目标全量数据表 - 存储雷达设备上报的目标探测时序数据)】的数据库操作Service
* @createDate 2026-04-11 15:37:16
*/
public interface DataRadarTargetService extends IService<DataRadarTarget> {

    /**
     * 批量保存数据
     */
    void saveBatchData(List<DataRadarTarget> dataTdoaTargets);

    List<DataRadarTarget> convertToDataRadarTargetList(String jsonString);
}
