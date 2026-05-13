package com.example.coreserver.service.business;


import com.example.coreserver.service.algorithm.AlgorithmDataProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 接口的一个简单默认实现，依赖空间关系计算模块的返回结果
 */
public class BaseAlertRulesEngine implements AlertRulesEngine {

    @Autowired
    AlgorithmDataProcessor algorithmDataProcessor;


    @Override
    public int getWarningLevel() {
        /**
         * 获取空间位置关系
         */
        int warningLevel = algorithmDataProcessor.getWarningLevel();

        /**
         * 预警等级计算
         */
        return warningLevel;
    }

    @Override
    public void getWarningStrategy() {
        /**
         * 获取空间位置关系
         */


        /**
         * 预警等策略计算
         */

    }
}
