package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.vo.target.TdoaTargetListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
* @author 70411
* @description 针对表【data_tdoa_target(TDOA目标全量数据表 - 存储TDOA无人机探测系统上报的目标时序数据)】的数据库操作Mapper
* @createDate 2026-04-11 15:37:16
* @Entity generator.domain.DataTdoaTarget
*/
@Mapper
public interface DataTdoaTargetMapper extends BaseMapper<DataTdoaTarget> {
    Page<TdoaTargetListItemVO> selectTdoaTargetPage(Page<TdoaTargetListItemVO> page,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime,
                                                    @Param("targetType") Integer targetType);

    Page<DataTdoaTarget> selectTdoaTrajectoryPage(Page<DataTdoaTarget> page,
                                                  @Param("uavId") String uavId,
                                                  @Param("traceId") String traceId);
}




