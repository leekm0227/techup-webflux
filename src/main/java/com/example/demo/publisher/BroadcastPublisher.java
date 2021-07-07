package com.example.demo.publisher;

import com.example.demo.manager.PosManager;
import com.example.demo.model.PayloadType;
import com.example.demo.model.ReceiveType;
import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BroadcastPublisher {
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap;
    private final ConcurrentHashMap<String, List<WebSocketSession>> channelMap;
    private final Sinks.Many<Request> sink;
    private final PosManager posManager;
    private final static int USER_SIZE = 10000;

    public BroadcastPublisher(PosManager posManager) {
        this.sessionMap = new ConcurrentHashMap<>();
        this.channelMap = new ConcurrentHashMap<>();
        this.sink = Sinks.many().multicast().onBackpressureBuffer(1);
        this.posManager = posManager;
    }

    public String getChannelId(String sessionId) {
        int index = new ArrayList<>(sessionMap.values()).indexOf(sessionMap.get(sessionId));
        String key = "channel" + Math.floorDiv(index, USER_SIZE);

        if (!channelMap.containsKey(key)) {
            channelMap.put(key, new ArrayList<>());
        }

        if (!channelMap.get(key).contains(sessionMap.get(sessionId))) {
            channelMap.get(key).add(sessionMap.get(sessionId));
        }

        return key;
    }

    public void join(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
//        test(session);
    }

    public void leave(WebSocketSession session) {
        posManager.leave(session.getId());
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
        return sink.asFlux()
                .onBackpressureBuffer(1)
                .parallel()
                .filter(request -> (request.getReceiveType() == ReceiveType.SESSION && request.getReceiver().equals(session.getId()))
                        || (request.getReceiveType() == ReceiveType.CHANNEL && channelMap.get(request.getReceiver()).contains(session))
                        || (request.getPayloadType() == PayloadType.START_TEST)
                );
    }
}
