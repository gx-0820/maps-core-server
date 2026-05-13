package com.example.coreserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.coreserver.dto.ExceptionLogConditionDTO;
import com.example.coreserver.entity.log.ExceptionLog;
import com.example.coreserver.mapper.ExceptionLogMapper;
import com.example.coreserver.service.log.ExceptionLogService;
import com.example.coreserver.vo.ExceptionLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lord
 * @date 2025/4/4
 * @description 异常日志模块服务实现类
 */
@Service
@RequiredArgsConstructor
public class ExceptionLogServiceImpl extends ServiceImpl<ExceptionLogMapper, ExceptionLog> implements ExceptionLogService {

    private final ExceptionLogMapper exceptionLogMapper;

    /**
     * 获取异常日志
     *
     * @param exceptionLogConditionDTO 查询条件
     * @return 异常日志
     */
    @Override
    public List<ExceptionLogVO> listExceptionLogs(ExceptionLogConditionDTO exceptionLogConditionDTO) {
//        return exceptionLogMapper.listExceptionLogs(new Page<>(exceptionLogConditionDTO.getCurrent(),exceptionLogConditionDTO.getSize()), exceptionLogConditionDTO);
        return exceptionLogMapper.listExceptionLogs(exceptionLogConditionDTO);
    }
}