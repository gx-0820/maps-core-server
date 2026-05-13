package com.example.coreserver.controller;

import com.example.coreserver.service.algorithm.AlgorithmDataProcessor;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 *  辅助决策接口
 * @author gaoxin
 */
@Slf4j
@RestController
@Api(tags = "辅助决策接口")
@RequestMapping("/api/decisionAid")
public class DecisionAidController {

    @Autowired
    private AlgorithmDataProcessor algorithmDataProcessor;

    // 简单轮询接口
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkThreat() {
        int warningLevel = algorithmDataProcessor.getWarningLevel();
        if (warningLevel >= 1 && warningLevel <= 3) {
            return "当前目标建议进行区域诱降处理";
        } else {
            return "当前目标暂不建议进行处理";
        }
    }

    // 实时推送接口（SSE）
//    @Operation(summary = "实时推送威胁状态和决策建议")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public SseEmitter threatStream() {
        SseEmitter emitter = new SseEmitter(60_000L); // 1分钟超时

        // 创建专用线程进行状态检查
        Thread updateThread = new Thread(() -> {
            try {
                String lastDecision = "";
                while (!Thread.currentThread().isInterrupted()) {
                    int warningLevel = algorithmDataProcessor.getWarningLevel();
                    String currentDecision = (warningLevel >= 1 && warningLevel <= 3) ? 
                            "当前目标建议进行区域诱降处理" : 
                            "当前目标暂不建议进行处理";

                    // 仅当状态变化时发送
                    if (!currentDecision.equals(lastDecision)) {
                        emitter.send(currentDecision);
                        lastDecision = currentDecision;
                        log.info("威胁等级: {}, 决策建议: {}", warningLevel, currentDecision);
                    }

                    // 降低CPU占用
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });

        // 配置生命周期回调
        emitter.onCompletion(() -> {
            updateThread.interrupt();
            log.info("SSE连接正常关闭");
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时关闭");
            updateThread.interrupt();
        });

        updateThread.start();
        return emitter;
    }
}