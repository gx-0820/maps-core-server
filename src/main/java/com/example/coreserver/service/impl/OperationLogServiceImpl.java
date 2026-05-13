package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.dto.OperationLogConditionDTO;
import com.example.coreserver.entity.log.OperationLog;
import com.example.coreserver.mapper.OperationLogMapper;
import com.example.coreserver.service.log.OperationLogService;
import com.example.coreserver.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lord
 * @date 2025/4/4
 * @description 操作日志业务实现类
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 查看操作日志
     *
     * @param operationLogConditionDTO 查询条件
     * @return 操作日志列表
     */
    @Override
    public List<OperationLogVO> listOperationLogs(OperationLogConditionDTO operationLogConditionDTO) {
//        return operationLogMapper.listOperationLogs(new Page<>(operationLogConditionDTO.getCurrent(), operationLogConditionDTO.getSize()), operationLogConditionDTO);
        return operationLogMapper.listOperationLogs(operationLogConditionDTO);
    }
}