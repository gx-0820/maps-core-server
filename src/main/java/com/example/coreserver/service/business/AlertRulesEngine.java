package com.example.coreserver.service.business;

/**
 * 定义规则引擎接口，提供两个功能
 * 1、返回预警等级
 * 2、返回预警策略
 */

public interface AlertRulesEngine {
    public int getWarningLevel();
    public void getWarningStrategy();
}
