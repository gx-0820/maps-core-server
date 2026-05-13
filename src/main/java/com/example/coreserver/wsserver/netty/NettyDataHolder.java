package com.example.coreserver.wsserver.netty;

import cn.hutool.json.JSONObject;
import com.example.coreserver.wsserver.base.WSType;
import com.example.coreserver.wsserver.pojo.WsSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.yeauty.pojo.Session;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketSession 用于保存当前所有在线的会话信息
 *
 */
@Service
public class NettyDataHolder {
    private final Map<String, WsSession> userSessionMap;// = Map.of();
    private static final char[] HEX_UPPER = "0123456789ABCDEF".toCharArray();
    private static final char[] HEX_LOWER = "0123456789abcdef".toCharArray();

    public NettyDataHolder() {
        this.userSessionMap = new ConcurrentHashMap<>();
    }


    public void add(String sessionKey, WsSession session) {
        removeSession(sessionKey);
        userSessionMap.put(sessionKey, session);
    }


    public void removeSession(String sessionKey) {
        userSessionMap.remove(sessionKey);
    }


    public WsSession get(String sessionKey) {
        return userSessionMap.get(sessionKey);
    }

    public void message(Session session, String message) {
        String sessionId = session.id().asLongText();
        WsSession wsSession = get(sessionId);
        wsSession.getService().message(message);
    }

    public void message(Session session, byte[] bytes) {
        String sessionId = session.id().asLongText();
        WsSession wsSession = get(sessionId);
        wsSession.getService().message(bytesToHexString(bytes, true));
    }

    /**
     * 发送指令
     *
     * @param deviceCode
     * @param command
     * @param args
     * @return
     */
    public void command(String deviceCode, String command, JSONObject args) {
        if(args ==null){
            args = new JSONObject();
        }

        args.set("type", command);
        String commandData = args.toJSONString(0);
        userSessionMap
                .values()
                .stream()
                .filter(e -> Objects.equals(e.getClientCode(), deviceCode))
                .filter(e -> e.getSession() != null)
                .findFirst()
                .ifPresent(e -> e.getSession().sendText(commandData));
    }


    /**
     * 数据发送到前端
     */
    public void forwardToWeb(JSONObject jsonObject) {
        this.userSessionMap.values()
                .stream()
                .filter(e ->
                        StringUtils.isNotBlank(e.getClientCode())
                                && e.getClientCode().startsWith(WSType.WEB.name()))
                .forEach(e -> e.getSession().sendText(jsonObject.toJSONString(0)));
    }


    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes     字节数组
     * @param upperCase 是否大写
     */
    public static String bytesToHexString(byte[] bytes, boolean upperCase) {
        if (bytes == null) return null;
        if (bytes.length == 0) return "";

        char[] hexArray = upperCase ? HEX_UPPER : HEX_LOWER;
        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
