package com.example.coreserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.vo.target.RadarTargetListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
* @author 70411
* @description 针对表【data_radar_target(雷达目标全量数据表 - 存储雷达设备上报的目标探测时序数据)】的数据库操作Mapper
* @createDate 2026-04-11 15:37:16
* @Entity generator.domain.DataRadarTarget
*/
@Mapper
public interface DataRadarTargetMapper extends BaseMapper<DataRadarTarget> {
    Page<RadarTargetListItemVO> selectRadarTargetPage(Page<RadarTargetListItemVO> page,
                                                      @Param("startDateTime") LocalDateTime startDateTime,
                                                      @Param("endDateTime") LocalDateTime endDateTime,
                                                      @Param("targetType") Integer targetType);

    Page<DataRadarTarget> selectRadarTrajectoryPage(Page<DataRadarTarget> page,
                                                    @Param("targetBatch") Long targetBatch,
                                                    @Param("targetId") Integer targetId);
}




