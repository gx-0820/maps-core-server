package com.example.coreserver.service.business;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coreserver.entity.DataFusionTarget;
import com.example.coreserver.entity.DataRadarTarget;
import com.example.coreserver.entity.DataTdoaTarget;
import com.example.coreserver.entity.PhotoelectricFileRecord;
import com.example.coreserver.mapper.DataFusionTargetMapper;
import com.example.coreserver.mapper.DataRadarTargetMapper;
import com.example.coreserver.mapper.DataTdoaTargetMapper;
import com.example.coreserver.service.PhotoelectricFileRecordService;
import com.example.coreserver.vo.target.FusionTargetListItemVO;
import com.example.coreserver.vo.target.RadarTargetListItemVO;
import com.example.coreserver.vo.target.TdoaTargetListItemVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TargetQueryService {

    private static final long DEFAULT_PAGE_NUM = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;

    private final DataFusionTargetMapper dataFusionTargetMapper;
    private final DataRadarTargetMapper dataRadarTargetMapper;
    private final DataTdoaTargetMapper dataTdoaTargetMapper;
    private final PhotoelectricFileRecordService photoelectricFileRecordService;

    public TargetQueryService(DataFusionTargetMapper dataFusionTargetMapper,
                              DataRadarTargetMapper dataRadarTargetMapper,
                              DataTdoaTargetMapper dataTdoaTargetMapper,
                              PhotoelectricFileRecordService photoelectricFileRecordService) {
        this.dataFusionTargetMapper = dataFusionTargetMapper;
        this.dataRadarTargetMapper = dataRadarTargetMapper;
        this.dataTdoaTargetMapper = dataTdoaTargetMapper;
        this.photoelectricFileRecordService = photoelectricFileRecordService;
    }

    public Page<RadarTargetListItemVO> pageRadarTargets(LocalDate queryDate, LocalTime startTime,
                                                        LocalTime endTime, Integer targetType,
                                                        Long pageNum, Long pageSize) {
        TargetQueryWindow window = buildQueryWindow(queryDate, startTime, endTime);
        Page<RadarTargetListItemVO> page = buildPage(pageNum, pageSize);
        return dataRadarTargetMapper.selectRadarTargetPage(page, window.startDateTime(), window.endDateTime(), targetType);
    }

    public Page<DataRadarTarget> pageRadarTrajectory(String compositeTargetId, Long pageNum, Long pageSize) {
        RadarTargetKey key = parseRadarTargetId(compositeTargetId);
        Page<DataRadarTarget> page = buildPage(pageNum, pageSize);
        return dataRadarTargetMapper.selectRadarTrajectoryPage(page, key.targetBatch(), key.targetId());
    }

    public List<PhotoelectricFileRecord> getRadarTargetOFDVideo(String targetId) {
        LambdaQueryWrapper<PhotoelectricFileRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PhotoelectricFileRecord::getTargetId, targetId);
        List<PhotoelectricFileRecord> list = photoelectricFileRecordService.list(queryWrapper);
        return list;
    }

    public Page<TdoaTargetListItemVO> pageTdoaTargets(LocalDate queryDate, LocalTime startTime,
                                                      LocalTime endTime, Integer targetType,
                                                      Long pageNum, Long pageSize) {
        TargetQueryWindow window = buildQueryWindow(queryDate, startTime, endTime);
        Page<TdoaTargetListItemVO> page = buildPage(pageNum, pageSize);
        return dataTdoaTargetMapper.selectTdoaTargetPage(page, window.startDateTime(), window.endDateTime(), targetType);
    }

    public Page<DataTdoaTarget> pageTdoaTrajectory(String compositeTargetId, Long pageNum, Long pageSize) {
        TdoaTargetKey key = parseTdoaTargetId(compositeTargetId);
        Page<DataTdoaTarget> page = buildPage(pageNum, pageSize);
        return dataTdoaTargetMapper.selectTdoaTrajectoryPage(page, key.uavId(), key.traceId());
    }

    public Page<FusionTargetListItemVO> pageFusionTargets(LocalDate queryDate, LocalTime startTime,
                                                          LocalTime endTime, String targetType,
                                                          Long pageNum, Long pageSize) {
        TargetQueryWindow window = buildQueryWindow(queryDate, startTime, endTime);
        Page<FusionTargetListItemVO> page = buildPage(pageNum, pageSize);
        return dataFusionTargetMapper.selectFusionTargetPage(page, window.startDateTime(), window.endDateTime(),
                targetType == null || targetType.isBlank() ? null : targetType.trim());
    }

    public Page<DataFusionTarget> pageFusionTrajectory(String targetId, Long pageNum, Long pageSize) {
        Long targetBatch = parseFusionTargetId(targetId);
        Page<DataFusionTarget> page = buildPage(pageNum, pageSize);
        return dataFusionTargetMapper.selectFusionTrajectoryPage(page, targetBatch);
    }

    private <T> Page<T> buildPage(Long pageNum, Long pageSize) {
        long current = pageNum == null ? DEFAULT_PAGE_NUM : pageNum;
        long size = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        if (current <= 0 || size <= 0) {
            throw new IllegalArgumentException("pageNum and pageSize must be greater than 0");
        }
        return new Page<>(current, size);
    }

    private RadarTargetKey parseRadarTargetId(String compositeTargetId) {
        String[] parts = splitCompositeTargetId(compositeTargetId, "targetId format must be targetBatch_targetId");
        try {
            return new RadarTargetKey(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("targetId format must be targetBatch_targetId");
        }
    }

    private TdoaTargetKey parseTdoaTargetId(String compositeTargetId) {
        String[] parts = splitCompositeTargetId(compositeTargetId, "targetId format must be uavId_traceId");
        return new TdoaTargetKey(parts[0], parts[1]);
    }

    private Long parseFusionTargetId(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId format must be targetBatch");
        }
        try {
            return Long.parseLong(targetId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("targetId format must be targetBatch");
        }
    }

    private TargetQueryWindow buildQueryWindow(LocalDate queryDate, LocalTime startTime, LocalTime endTime) {
        LocalDate actualDate = queryDate == null ? LocalDate.now() : queryDate;
        LocalTime actualStartTime = startTime == null ? LocalTime.MIN : startTime;
        LocalDateTime startDateTime = actualDate.atTime(actualStartTime);

        LocalDateTime endDateTime;
        if (endTime == null) {
            endDateTime = actualDate.plusDays(1).atStartOfDay();
        } else {
            if (startTime != null && endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("endTime must be greater than or equal to startTime");
            }
            endDateTime = actualDate.atTime(endTime).plusMinutes(1);
        }

        return new TargetQueryWindow(startDateTime, endDateTime);
    }

    private String[] splitCompositeTargetId(String compositeTargetId, String formatMessage) {
        if (compositeTargetId == null || compositeTargetId.isBlank()) {
            throw new IllegalArgumentException(formatMessage);
        }

        String[] parts = compositeTargetId.split("_", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException(formatMessage);
        }
        return parts;
    }

    private record RadarTargetKey(Long targetBatch, Integer targetId) {
    }

    private record TdoaTargetKey(String uavId, String traceId) {
    }

    private record TargetQueryWindow(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    }
}
