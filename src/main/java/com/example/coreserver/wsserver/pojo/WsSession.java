package com.example.coreserver.wsserver.pojo;

import com.example.coreserver.wsserver.base.AbsDeviceProcess;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.MultiValueMap;
import org.yeauty.pojo.Session;

import java.util.Map;

@Builder
@Data
public class WsSession {
    private String clientCode;
    private Session session;
    private AbsDeviceProcess service;

    // ws 连接的参数
    private String req;
    private MultiValueMap reqMap;
    private String arg;
    private Map pathMap;
}