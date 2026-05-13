package com.example.coreserver.service.business;

import com.example.coreserver.dto.DroneDailyStatsResponse;
import com.example.coreserver.dto.DroneStatsTrendPoint;
import com.example.coreserver.entity.TargetMonitorStatEntity;
import com.example.coreserver.repository.TargetMonitorStatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TargetMonitorStatsService {

    private static final int DEFAULT_TREND_DAYS = 7;

    private final TargetMonitorStatRepository targetMonitorStatRepository;

    public TargetMonitorStatsService(TargetMonitorStatRepository targetMonitorStatRepository) {
        this.targetMonitorStatRepository = targetMonitorStatRepository;
    }

    public DroneDailyStatsResponse getDailyStats(LocalDate date) {
        LocalDate queryDate = date == null ? LocalDate.now() : date;
        return targetMonitorStatRepository.findByStatTime(queryDate)
                .map(this::toDailyStats)
                .orElseGet(() -> emptyDailyStats(queryDate));
    }

    public List<DroneStatsTrendPoint> getTrendStats(LocalDate endDate) {
        LocalDate actualEndDate = endDate == null ? LocalDate.now() : endDate;
        LocalDate startDate = actualEndDate.minusDays(DEFAULT_TREND_DAYS - 1L);

        Map<LocalDate, TargetMonitorStatEntity> statsByDate = targetMonitorStatRepository
                .findByStatTimeBetweenOrderByStatTimeAsc(startDate, actualEndDate)
                .stream()
                .collect(Collectors.toMap(TargetMonitorStatEntity::getStatTime, Function.identity()));

        List<DroneStatsTrendPoint> trend = new ArrayList<>(DEFAULT_TREND_DAYS);
        for (LocalDate date = startDate; !date.isAfter(actualEndDate); date = date.plusDays(1)) {
            TargetMonitorStatEntity entity = statsByDate.get(date);
            trend.add(entity == null ? emptyTrendPoint(date) : toTrendPoint(entity));
        }
        return trend;
    }

    private DroneDailyStatsResponse toDailyStats(TargetMonitorStatEntity entity) {
        return new DroneDailyStatsResponse(
                entity.getStatTime(),
                defaultZero(entity.getRadarTargetCount()),
                defaultZero(entity.getTdoaTargetCount()),
                defaultZero(entity.getFusionTargetCount()),
                defaultZero(entity.getRadarIllegalCount()),
                defaultZero(entity.getTdoaIllegalCount()),
                defaultZero(entity.getFusionIllegalCount()),
                defaultZero(entity.getNeedDisposeCount()),
                defaultZero(entity.getEffectiveDisposeCount())
        );
    }

    private DroneStatsTrendPoint toTrendPoint(TargetMonitorStatEntity entity) {
        return new DroneStatsTrendPoint(
                entity.getStatTime(),
                defaultZero(entity.getRadarTargetCount()),
                defaultZero(entity.getTdoaTargetCount()),
                defaultZero(entity.getFusionTargetCount()),
                defaultZero(entity.getRadarIllegalCount()),
                defaultZero(entity.getTdoaIllegalCount()),
                defaultZero(entity.getFusionIllegalCount()),
                defaultZero(entity.getNeedDisposeCount()),
                defaultZero(entity.getEffectiveDisposeCount())
        );
    }

    private DroneDailyStatsResponse emptyDailyStats(LocalDate date) {
        return new DroneDailyStatsResponse(date, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private DroneStatsTrendPoint emptyTrendPoint(LocalDate date) {
        return new DroneStatsTrendPoint(date, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
