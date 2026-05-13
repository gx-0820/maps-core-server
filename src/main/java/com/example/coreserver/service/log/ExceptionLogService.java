package com.example.coreserver.service.log;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.coreserver.dto.ExceptionLogConditionDTO;
import com.example.coreserver.entity.log.ExceptionLog;
import com.example.coreserver.vo.ExceptionLogVO;

import java.util.List;

/**
 * @author lord
 * @date 2025/4/4
 * @description 异常日志模块服务接口类
 */
public interface ExceptionLogService extends IService<ExceptionLog> {

    /**
     * 获取异常日志
     *
     * @param exceptionLogConditionDTO 查询条件
     * @return 异常日志
     */
    List<ExceptionLogVO> listExceptionLogs(ExceptionLogConditionDTO exceptionLogConditionDTO);
}
