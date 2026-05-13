package com.example.coreserver.service.business;

import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrontendSessionManager {
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public static void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public static void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public static Map<String, WebSocketSession> getSessions() {
        return sessions;
    }
}