package com.example.coreserver.service.impl;

import com.example.coreserver.dto.CountermeasureRequest;
import com.example.coreserver.service.business.CountermeasureService;
import com.example.coreserver.service.countermeasure.CountermeasureAutoTaskService;
import com.example.coreserver.service.countermeasure.CountermeasureConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountermeasureServiceImpl implements CountermeasureService {

    private final CountermeasureConfigService countermeasureConfigService;
    private final CountermeasureAutoTaskService countermeasureAutoTaskService;

    @Override
    public String handleThreat(CountermeasureRequest request) {
        countermeasureAutoTaskService.triggerImmediateRound();
        return "自动处置已切换为后台轮次任务，当前模式=" + countermeasureConfigService.getMode() + "，已触发一次即时轮次";
    }
}
