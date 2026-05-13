package com.example.coreserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.DataTdoaTarget;

/**
* @author 70411
* @description 针对表【data_tdoa_target(TDOA目标全量数据表 - 存储TDOA无人机探测系统上报的目标时序数据)】的数据库操作Service
* @createDate 2026-04-11 15:37:16
*/
public interface DataTdoaTargetService extends IService<DataTdoaTarget> {

    /**
     * 批量保存数据
     * @param dataTdoaTarget
     */
    void saveBatchData(DataTdoaTarget dataTdoaTarget);
}
