package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.coreserver.dto.OperationLogConditionDTO;
import com.example.coreserver.vo.OperationLogVO;
import com.example.coreserver.entity.log.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lord
 * @date 2025/4/4
 * @description
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    /**
     * 查看操作日志
     *
     * @param page                    分页信息
     * @param operationLogConditionDTO 查询条件
     * @return 操作日志列表
     */
//    Page<OperationLogVO> listOperationLogs(@Param("page") Page<Object> page, @Param("operationLogConditionVO") OperationLogConditionDTO operationLogConditionDTO);
    List<OperationLogVO> listOperationLogs(@Param("operationLogConditionDTO") OperationLogConditionDTO operationLogConditionDTO);
}