package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreserver.entity.DataFusionTarget;
import com.example.coreserver.vo.target.FusionTargetListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface DataFusionTargetMapper extends BaseMapper<DataFusionTarget> {
    Page<FusionTargetListItemVO> selectFusionTargetPage(Page<FusionTargetListItemVO> page,
                                                        @Param("startDateTime") LocalDateTime startDateTime,
                                                        @Param("endDateTime") LocalDateTime endDateTime,
                                                        @Param("targetType") String targetType);

    Page<DataFusionTarget> selectFusionTrajectoryPage(Page<DataFusionTarget> page,
                                                      @Param("targetBatch") Long targetBatch);
}
