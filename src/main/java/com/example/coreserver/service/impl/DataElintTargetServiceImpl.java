package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.entity.DataElintTarget;
import com.example.coreserver.mapper.DataElintTargetMapper;
import com.example.coreserver.service.DataElintTargetService;
import com.example.coreserver.utils.DataBatchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 电侦目标数据服务实现类
 */
@Slf4j
@Service
public class DataElintTargetServiceImpl extends ServiceImpl<DataElintTargetMapper, DataElintTarget>
    implements DataElintTargetService {

    public static final String BUSINESS_KEY = "DataElintTarget";

    private final DataBatchUtils<DataElintTarget> dataBatchUtils;

    public DataElintTargetServiceImpl(DataBatchUtils<DataElintTarget> dataBatchUtils) {
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
    public void saveBatchData(DataElintTarget dataElintTarget) {
        if (dataElintTarget == null) {
            return;
        }

        // 添加到批量队列
        dataBatchUtils.addAll(BUSINESS_KEY, List.of(dataElintTarget));
    }


    /**
     * 批量保存回调方法
     *
     * @param dataList 数据列表
     */
    private void batchSave(List<DataElintTarget> dataList) {
        try {
            // 使用MyBatis-Plus的saveBatch方法
            boolean result = this.saveBatch(dataList);
            if (result) {
                log.info("电侦目标批量保存成功，数量: {}", dataList.size());
            } else {
                log.error("电侦目标批量保存失败");
                throw new RuntimeException("电侦目标批量保存失败");
            }
        } catch (Exception e) {
            log.error("电侦目标批量保存异常", e);
            throw new RuntimeException("电侦目标批量保存异常", e);
        }
    }

}