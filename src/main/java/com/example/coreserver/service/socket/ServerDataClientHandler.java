package com.example.coreserver.service.socket;

import cn.hutool.json.JSONObject;
import com.example.coreserver.service.device.RadarService;
import com.example.coreserver.wsserver.netty.NettyDataHolder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ServerDataClientHandler extends AbstractWebSocketHandler {

    private final RadarService radarService;
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ApplicationContext applicationContext;
    private final NettyDataHolder nettyDataHolder;


    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public ServerDataClientHandler(RadarService radarService, ApplicationContext applicationContext, NettyDataHolder nettyDataHolder) {
        this.radarService = radarService;
        this.applicationContext = applicationContext;
        this.nettyDataHolder = nettyDataHolder;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received text message from session {}: {}", session.getId(), payload);
        processMessage(session, payload);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 将二进制消息转换为字符串
        byte[] bytes = message.getPayload().array();
        String payload = new String(bytes, StandardCharsets.UTF_8);
        log.info("Received binary message from session {}: {}", session.getId(), payload);
        processMessage(session, payload);
    }

    private void processMessage(WebSocketSession session, String message) {
        try {
            // 处理业务逻辑
            log.info("Processing message: {}", message);
            // 如果需要回复
            // session.sendMessage(new TextMessage("{\"status\": \"ok\"}"));

            applicationContext.getBean(DataService.class).handelMessage(message, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("Connection closed: {} | Reason: {}", session.getId(), status.getReason());
        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket error on session {}: {}", session.getId(), exception.getMessage(), exception);
        sessions.remove(session.getId());
    }


    /**
     * 广播消息给所有连接的客户端（JSONObject 格式）
     *
     * @param type 类型
     * @param obj  数据
     */
    public void broadcast(String type, Object obj) {
        // 数据
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("dataType", type);
        jsonObject.set("data", obj);

        nettyDataHolder.forwardToWeb(jsonObject);


//        JSONObject jsonObject = new JSONObject();
//        jsonObject.set("dataType", type);
//        jsonObject.set("data", obj);
//        WebSocketMessage<String> message = new TextMessage(jsonObject.toJSONString(0));
//        sessions.values().forEach(session -> {
//            executor.submit(() -> {
//                try {
//                    session.sendMessage(message);
//                } catch (IOException e) {
//                    log.error("Failed to send message: {}", message, e);
//                    log.debug("broadcast called by thread: {}", Thread.currentThread().getName());
//                }
//            });
//
//        });
    }


//    /**
//     * 发送雷达实时目标数据
//     */
//    @Scheduled(fixedRate = 200)
//    public void sendDronesListMessage() {
//
//        if (sessions.isEmpty()) {
//            return;
//        }
//
//        DeviceId deviceId = DeviceId.newBuilder().setDeviceId("RADAR01").build(); // 使用实际的设备ID
//        List<DroneVO> radarTargets = radarService.getRadarTargetsAsDroneVO(deviceId);
//        if (radarTargets == null || radarTargets.isEmpty()) {
//            return;
//        }
//        broadcast(Constant.DEVICE_RADAR,radarTargets);
//    }
}