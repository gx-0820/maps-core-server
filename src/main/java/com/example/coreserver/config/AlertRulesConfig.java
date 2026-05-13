package com.example.coreserver.config;

import com.example.coreserver.service.business.BaseAlertRulesEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertRulesConfig {

    @Bean
    public BaseAlertRulesEngine getBaseAlertRulesEngine() {
        return new BaseAlertRulesEngine();
    }
}
