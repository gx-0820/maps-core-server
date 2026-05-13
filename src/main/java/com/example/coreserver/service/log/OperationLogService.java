package com.example.coreserver.service.log;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.dto.OperationLogConditionDTO;
import com.example.coreserver.entity.log.OperationLog;
import com.example.coreserver.vo.OperationLogVO;

import java.util.List;

/**
 * @author lord
 * @date 2025/4/4
 * @description 操作日志业务接口类
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 查看操作日志
     * @param operationLogConditionDTO 查询条件
     * @return 操作日志列表
     */
    List<OperationLogVO> listOperationLogs(OperationLogConditionDTO operationLogConditionDTO);
}