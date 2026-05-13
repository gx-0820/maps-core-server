package com.example.coreserver.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.coreserver.service.socket.ServerDataClientHandler;
import io.swagger.annotations.Api;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 操作日志输出接口
 *
 * @author gaoxin
 */
@RestController
@Api(tags = "操作日志输出接口")
@RequestMapping("/api/sse")
public class OperationSseController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ServerDataClientHandler serverDataClientHandler;

    public OperationSseController(ServerDataClientHandler serverDataClientHandler) {
        this.serverDataClientHandler = serverDataClientHandler;
    }

    @GetMapping(value = "/operations", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        emitters.add(emitter);

        // 发送初始连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connection"));
//                .data(new OperationEvent("系统操作日志启动")));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void sendOperation(String operation) {
//        List<SseEmitter> deadEmitters = new ArrayList<>();
//
//        emitters.forEach(emitter -> {
//            try {
//                emitter.send(SseEmitter.event()
//                    .name("operation")
//                    .data(new OperationEvent(operation)));
//            } catch (IOException e) {
//                deadEmitters.add(emitter);
//            }
//        });
//
//        emitters.removeAll(deadEmitters);
        JSONObject target = new JSONObject();
        target.put("id", "operation");
        target.put("operation", operation);
        target.put("time", DateUtil.format(new Date(), DatePattern.NORM_DATETIME_MS_FORMAT));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("target", target);

        serverDataClientHandler.broadcast("OPERATION", jsonObject);
    }

    @Getter
    private static class OperationEvent {
        private final String operation;
        private final long timestamp;

        public OperationEvent(String operation) {
            this.operation = operation;
            this.timestamp = System.currentTimeMillis();
        }

    }
} 