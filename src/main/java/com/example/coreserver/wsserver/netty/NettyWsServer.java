package com.example.coreserver.wsserver.netty;


import com.example.coreserver.wsserver.WSConstant;
import com.example.coreserver.wsserver.base.AbsDeviceProcess;
import com.example.coreserver.wsserver.base.WSType;
import com.example.coreserver.wsserver.pojo.WsSession;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.coreserver.wsserver.WSConstant.WS_PATH_PARAMETER_NAME;

@Slf4j
@EnableWebSocket
@Component
@ServerEndpoint(path = "/ws/core/{code}", port = WSConstant.port)
public class NettyWsServer {

    private NettyDataHolder nettyDataHolder;
    private ApplicationContext applicationContext;

    /**
     * 设备参数
     * web 前端请求
     * xx
     */

    private final Map<String, AbsDeviceProcess> services = new ConcurrentHashMap<>();

    public NettyWsServer(){}
//    public NettyWsServer(NettyDataHolder nettyDataHolder, ApplicationContext applicationContext) {
//        this.nettyDataHolder = nettyDataHolder;
//        this.applicationContext = applicationContext;
//        applicationContext.getBeansOfType(AbsDeviceProcess.class)
//                .values()
//                .forEach(a -> services.put(a.code().name(), a));
//    }

    @Autowired
    public void setNettyDataHolder(NettyDataHolder nettyDataHolder) {
        this.nettyDataHolder = nettyDataHolder;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // 初始化 services
        applicationContext.getBeansOfType(AbsDeviceProcess.class)
                .values()
                .forEach(a -> services.put(a.code().name(), a));
    }

    @BeforeHandshake
    public void handshake(Session session,
                          @RequestParam String req,
                          @RequestParam MultiValueMap reqMap,
                          @PathVariable String arg,
                          @PathVariable Map pathMap) {
        System.out.printf("handshake");
    }


    @OnOpen
    public void onOpen(Session session,
                       @RequestParam String req,
                       @RequestParam MultiValueMap reqMap,
                       @PathVariable String arg,
                       @PathVariable Map pathMap) {
        Object o = pathMap.get(WS_PATH_PARAMETER_NAME);

        if (o == null) {
            return;
        }

        if(services.isEmpty()){
            applicationContext.getBeansOfType(AbsDeviceProcess.class)
                    .values()
                    .forEach(a -> services.put(a.code().name(), a));
        }

        String deviceCode = (String) o;
        String sessId = session.id().asLongText();

        AbsDeviceProcess absDeviceProcess = services.get(deviceCode);
        if(absDeviceProcess == null){
            absDeviceProcess = services.get(WSType.DEVICE.name());
        }

        WsSession wsSession = WsSession.builder()
                .clientCode(deviceCode)
                .session(session)
                .service(absDeviceProcess)
                .req(req)
                .arg(arg)
                .pathMap(pathMap)
                .reqMap(reqMap)
                .build();

        nettyDataHolder.add(sessId, wsSession);

        log.info("[connect] 连接成功. sessionId: {}  device: {}", session.id(), deviceCode);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String sessId = session.id().asLongText();
        nettyDataHolder.removeSession(sessId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        nettyDataHolder.message(session, message);
    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        nettyDataHolder.message(session, bytes);
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println("read idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("all idle");
                    break;
                default:
                    break;
            }
        }
    }

}
