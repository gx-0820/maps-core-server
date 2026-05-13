package com.example.coreserver.service;

import com.example.coreserver.entity.DataOfd;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.entity.DataTdoaTarget;
import com.fasterxml.jackson.databind.JsonNode;

/**
* @author 70411
* @description 针对表【data_ofd(光电设备数据表)】的数据库操作Service
* @createDate 2026-04-15 16:23:05
*/
public interface DataOfdService extends IService<DataOfd> {


    /**
     * 批量保存数据
     */
    void saveBatchData(DataOfd dataOfd);

    DataOfd convertToDataOfd(JsonNode deviceStatusNode);
}
