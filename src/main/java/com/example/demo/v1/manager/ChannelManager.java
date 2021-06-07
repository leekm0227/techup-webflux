package com.example.demo.v1.manager;

import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    private final ConcurrentHashMap<String, List<WebSocketSession>> channelMap = new ConcurrentHashMap<>();
    private final SessionManager sessionManager;

    public ChannelManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String createChannel(String sessionId) {
        String channelId = UUID.randomUUID().toString();
        List<WebSocketSession> sessions = new ArrayList<>();
        sessions.add(sessionManager.getBySessionId(sessionId));
        channelMap.put(channelId, sessions);
        return channelId;
    }

    public void joinChannel(String channelId, String sessionId) {
        channelMap.get(channelId).add(sessionManager.getBySessionId(sessionId));
    }

    public void leaveChannel(String channelId, String sessionId) {
        channelMap.get(channelId).remove(sessionManager.getBySessionId(sessionId));
    }

    public List<String> getList(){
        return new ArrayList<>(channelMap.keySet());
    }

    public void broadcast(Request request) {
        channelMap.computeIfPresent(request.getPayload().getReceiver(), (channelId, sessions) -> {
            sessions.forEach(session -> session.send(Mono.just(request.getPayload().getBody()).map(session::textMessage)));
            return sessions;
        });
    }
}
