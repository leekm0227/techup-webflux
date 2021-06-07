package com.example.demo.v1.manager;

import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void add(WebSocketSession session) {
        sessionMap.putIfAbsent(session.getId(), session);
    }

    public void remove(WebSocketSession session) {
        sessionMap.remove(session.getId());
    }

    public WebSocketSession getBySessionId(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public void broadcast(Request request) {
        sessionMap.computeIfPresent(request.getPayload().getReceiver(), (sessionId, session) -> {
            session.send(Mono.just(request.getPayload().getBody()).map(session::textMessage));
            return session;
        });
    }
}
