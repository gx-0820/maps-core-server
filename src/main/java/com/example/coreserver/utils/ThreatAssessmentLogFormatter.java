package com.example.coreserver.utils;

import com.example.coreserver.entity.threat.ThreatAssessmentArgs;
import com.example.coreserver.entity.threat.ThreatAssessmentResult;

/**
 * 威胁评估日志格式化工具。
 */
final class ThreatAssessmentLogFormatter {

    private ThreatAssessmentLogFormatter() {
    }

    static String describeArgs(ThreatAssessmentArgs args) {
        return String.format(
                "targetId=%s,targetType=%s,timestamp=%s,lon=%s,lat=%s,alt=%s,speed=%s,whiteList=%s,imageTransmission=%s",
                args.getId(),
                args.getTargetType(),
                args.getTimestamp(),
                args.getLongitude(),
                args.getLatitude(),
                args.getAltitude(),
                args.getSpeed(),
                args.isWhiteList(),
                args.isImageTransmission()
        );
    }

    static String describeResult(ThreatAssessmentResult result) {
        if (result == null) {
            return "null";
        }
        return String.format(
                "area=%s,level=%s,score=%s,whiteList=%s",
                result.getThreatAssessmentArea(),
                result.getThreatLevel(),
                result.getThreatScore(),
                result.isWhiteList()
        );
    }
}
