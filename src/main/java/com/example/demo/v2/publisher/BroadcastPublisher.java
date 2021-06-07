package com.example.demo.v2.publisher;

import com.example.demo.model.Payload;
import com.example.demo.model.PayloadType;
import com.example.demo.model.ReceiveType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BroadcastPublisher {
    private ConcurrentHashMap<String, WebSocketSession> sessionMap;
    private ConcurrentHashMap<String, List<WebSocketSession>> channelMap;
    private Sinks.Many<Payload> sink;

    @PostConstruct
    void init() {
        this.sessionMap = new ConcurrentHashMap<>();
        this.channelMap = new ConcurrentHashMap<>();
        this.sink = Sinks.many().replay().limit(1);
    }

    public void join(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
    }

    public void leave(WebSocketSession session) {
        sessionMap.remove(session.getId());
    }

    public String createChannel(String sessionId) {
        String channelId = UUID.randomUUID().toString();
        List<WebSocketSession> sessions = new ArrayList<>();
        sessions.add(sessionMap.get(sessionId));
        channelMap.put(channelId, sessions);
        return channelId;
    }

    public void joinChannel(String channelId, String sessionId) {
        channelMap.get(channelId).add(sessionMap.get(sessionId));
    }

    public void leaveChannel(String channelId, String sessionId) {
        channelMap.get(channelId).remove(sessionMap.get(sessionId));
    }

    public List<String> getList() {
        return new ArrayList<>(channelMap.keySet());
    }

    public void next(Payload payload) {
        sink.tryEmitNext(payload);
    }

    public Flux<Payload> subscribe(WebSocketSession session) {
        return sink.asFlux().filter(payload -> (payload.getReceiveType() == ReceiveType.SESSION && payload.getReceiver().equals(session.getId()))
                || (payload.getReceiveType() == ReceiveType.CHANNEL && channelMap.get(payload.getReceiver()).contains(session)));

    }


}
