package com.example.coreserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.mapper.DataTdoaTargetMapper;
import com.example.coreserver.service.DataTdoaTargetService;
import com.example.coreserver.utils.DataBatchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author 70411
* @description 针对表【data_tdoa_target(TDOA目标全量数据表 - 存储TDOA无人机探测系统上报的目标时序数据)】的数据库操作Service实现
* @createDate 2026-04-11 15:37:16
*/
@Slf4j
@Service
public class DataTdoaTargetServiceImpl extends ServiceImpl<DataTdoaTargetMapper, DataTdoaTarget>
    implements DataTdoaTargetService {

    public static final String BUSINESS_KEY = "DataTdoaTarget";

    private final DataBatchUtils<DataTdoaTarget> dataBatchUtils;

    public DataTdoaTargetServiceImpl(DataBatchUtils<DataTdoaTarget> dataBatchUtils) {
        this.dataBatchUtils = dataBatchUtils;
        // 注册批量处理器
        dataBatchUtils.register(
                BUSINESS_KEY,
                this::batchSave,  // 保存回调方法
                1000,             // 最大批量大小
                5000              // 刷新间隔5秒
        );
    }


    @Override
    public void saveBatchData(DataTdoaTarget dataTdoaTarget) {
        if (dataTdoaTarget == null) {
            return;
        }

        // 添加到批量队列
        dataBatchUtils.addAll(BUSINESS_KEY, List.of(dataTdoaTarget));
    }


    /**
     * 批量保存回调方法
     *
     * @param dataList
     */
    private void batchSave(List<DataTdoaTarget> dataList) {
        try {
            // 使用MyBatis-Plus的saveBatch方法
            boolean result = this.saveBatch(dataList);
            if (result) {
                log.info("批量保存成功，数量: {}", dataList.size());
            } else {
                log.error("批量保存失败");
                throw new RuntimeException("批量保存失败");
            }
        } catch (Exception e) {
            log.error("批量保存异常", e);
            throw new RuntimeException("批量保存异常", e);
        }
    }

}




