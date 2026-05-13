package com.example.coreserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.DataElintTarget;

/**
 * 电侦目标数据服务接口
 */
public interface DataElintTargetService extends IService<DataElintTarget> {

    /**
     * 批量保存数据
     * @param dataElintTarget 电侦目标数据
     */
    void saveBatchData(DataElintTarget dataElintTarget);
}