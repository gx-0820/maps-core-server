package com.example.coreserver.service.business;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertRulesService {
    @Autowired
    AlertRulesEngine alertRulesEngine;
    public int getWarningLevel() {
        return alertRulesEngine.getWarningLevel();
    }
    public void getWarningStrategy() {

    }

}
