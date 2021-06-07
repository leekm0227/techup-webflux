package com.example.demo.v1.manager;

import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    private final ConcurrentHashMap<String, List<Sinks.Many<Request>>> channelMap = new ConcurrentHashMap<>();
    private final SessionManager sessionManager;

    public ChannelManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String createChannel(String sessionId) {
        String channelId = Integer.toString(ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()).getInt(), 9);
        List<Sinks.Many<Request>> sessions = new ArrayList<>();
        sessions.add(sessionManager.getBySessionId(sessionId));
        channelMap.put(channelId, sessions);
        return channelId;
    }

    public void joinChannel(String channelId, String sessionId) {
        List<Sinks.Many<Request>> list = channelMap.get(channelId);

        if (list != null) {
            list.add(sessionManager.getBySessionId(sessionId));
        }
    }

    public void leaveChannel(String channelId, String sessionId) {
        List<Sinks.Many<Request>> list = channelMap.get(channelId);

        if (list != null) {
            list.remove(sessionManager.getBySessionId(sessionId));

            if (list.size() == 0) {
                channelMap.remove(channelId);
            }
        }
    }

    public List<String> getList() {
        return new ArrayList<>(channelMap.keySet());
    }

    public void broadcast(Request request) {
        channelMap.computeIfPresent(request.getReceiver(), (channelId, subscribers) -> {
            subscribers.forEach(subscriber -> subscriber.tryEmitNext(request));
            return subscribers;
        });
    }
}
