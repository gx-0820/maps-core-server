package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.coreserver.dto.ExceptionLogConditionDTO;
import com.example.coreserver.entity.log.ExceptionLog;
import com.example.coreserver.vo.ExceptionLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lord
 * @date 2025/4/4
 * @description 异常日志模块持久层类
 */
@Mapper
public interface ExceptionLogMapper extends BaseMapper<ExceptionLog> {

    /**
     * 获取异常日志
     * @param page 分页
     * @param exceptionLogConditionDTO 查询条件
     * @return 异常日志
     */
//    Page<ExceptionLogVO> listExceptionLogs(Page<Object> page, ExceptionLogConditionDTO exceptionLogConditionDTO);
    List<ExceptionLogVO> listExceptionLogs(@Param("exceptionLogConditionDTO") ExceptionLogConditionDTO exceptionLogConditionDTO);


//    int insert(ExceptionLog exceptionLog);
}