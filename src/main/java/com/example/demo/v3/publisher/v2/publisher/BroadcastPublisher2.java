package com.example.demo.v3.publisher.v2.publisher;

import com.example.demo.model.PayloadType;
import com.example.demo.model.ReceiveType;
import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BroadcastPublisher2 {
    private ConcurrentHashMap<String, WebSocketSession> sessionMap;
    private ConcurrentHashMap<String, List<WebSocketSession>> channelMap;
    private Sinks.Many<Request> sink;

    @PostConstruct
    void init() {
        this.sessionMap = new ConcurrentHashMap<>();
        this.channelMap = new ConcurrentHashMap<>();
        this.sink = Sinks.many().multicast().onBackpressureBuffer(1);
        for (int i = 0; i < 50; i++) {
            channelMap.put("channel" + i, new ArrayList<>());
        }
    }

    private void ready(WebSocketSession session) {
        ArrayList<WebSocketSession> sessionList = new ArrayList<>(sessionMap.values());
        int index = sessionList.indexOf(session);
        String key = "channel" + Math.floorDiv(index, 20);
        channelMap.get(key).add(session);
        Request request = new Request();
        request.setPayloadType(PayloadType.START_TEST);
        request.setReceiver(key);
        request.setChannelId(key);
        request.setTxtime(System.currentTimeMillis());
        this.next(request);
    }

    public void join(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        ready(session);
    }

    public void leave(WebSocketSession session) {
        sessionMap.remove(session.getId());
    }

    public String createChannel(String sessionId) {
        String channelId = UUID.randomUUID().toString().replace("-", "");
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

    public void next(Request request) {
        sink.tryEmitNext(request);
    }

    public ParallelFlux<Request> subscribe(WebSocketSession session) {
        return sink.asFlux().onBackpressureBuffer(1).parallel().filter(request -> (request.getReceiveType() == ReceiveType.SESSION && request.getReceiver().equals(session.getId()))
                || (request.getReceiveType() == ReceiveType.CHANNEL && channelMap.get(request.getReceiver()).contains(session))
                || (request.getPayloadType() == PayloadType.START_TEST)
        );
    }
}