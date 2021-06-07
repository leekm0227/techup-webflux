package com.example.demo.v1.manager;

import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final ConcurrentHashMap<String, Sinks.Many<Request>> sessionMap = new ConcurrentHashMap<>();

    public void add(String sessionId, Sinks.Many<Request> subscriber) {
        sessionMap.putIfAbsent(sessionId, subscriber);
    }

    public void remove(WebSocketSession session) {
        sessionMap.remove(session.getId());
    }

    public Sinks.Many<Request> getBySessionId(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public void broadcast(Request request) {
        sessionMap.computeIfPresent(request.getReceiver(), (sessionId, subscriber) -> {
            subscriber.tryEmitNext(request);
            return subscriber;
        });
    }
}
