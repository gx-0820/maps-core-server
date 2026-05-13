package com.example.coreserver.controller;

import com.example.coreserver.entity.countermeasure.CountermeasureEvent;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: gaoxin
 * @Description: 反制事件控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/events")
@Api(tags = "反制事件控制器")
@RequiredArgsConstructor
public class CountermeasureEventController {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('DEVICE_MANIPULATE')")
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter(360_000L); // 6分钟超时
        String clientId = UUID.randomUUID().toString();
        emitters.put(clientId, emitter);

        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));

        // 发送初始连接事件
        sendInitialEvent(emitter);
        return emitter;
    }

    @EventListener
    public void handleCountermeasureEvent(CountermeasureEvent event) {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(event.getTimestamp()))
                        .name(event.getType().name())
                        .data(event));
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        });
    }

    private void sendInitialEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Event stream connected"));
        } catch (IOException e) {
            log.error("初始化事件发送失败", e);
        }
    }
}