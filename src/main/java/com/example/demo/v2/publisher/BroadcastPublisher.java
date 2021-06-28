package com.example.demo.v2.publisher;

import com.example.demo.model.PayloadType;
import com.example.demo.model.ReceiveType;
import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BroadcastPublisher {
    private ConcurrentHashMap<String, WebSocketSession> sessionMap;
    private ConcurrentHashMap<String, List<WebSocketSession>> channelMap;
    private Sinks.Many<Request> sink;
    private ArrayList<WebSocketSession> sessionList = null;

    @PostConstruct
    void init() {
        this.sessionMap = new ConcurrentHashMap<>();
        this.channelMap = new ConcurrentHashMap<>();
        this.sink = Sinks.many().replay().limit(1);
        for (int i = 0; i < 50; i++) {
            channelMap.put("channel" + i, new ArrayList<>());
        }
    }

    private void ready(){
        ArrayList<WebSocketSession> sessionList = new ArrayList<>(sessionMap.values());

        if (sessionList.size() >= 10) {
            if (this.sessionList == null) {
                this.sessionList = sessionList;

                for (WebSocketSession session : sessionList) {
                    int index = sessionList.indexOf(session);
                    String key = "channel" + Math.floorDiv(index, 20);
                    channelMap.get(key).add(session);
                }

                for (String key : channelMap.keySet()) {
                    if(channelMap.get(key).size() > 0) {
                        Request request = new Request();
                        request.setPayloadType(PayloadType.START_TEST);
                        request.setReceiver(key);
                        request.setChannelId(key);
                        request.setTxtime(System.currentTimeMillis());
                        this.next(request);
                    }
                }
            }
        }
    }

    public void join(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
        ready();
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

    public Flux<Request> subscribe(WebSocketSession session) {
        return sink.asFlux().filter(request -> (request.getReceiveType() == ReceiveType.SESSION && request.getReceiver().equals(session.getId()))
                || (request.getReceiveType() == ReceiveType.CHANNEL && channelMap.get(request.getReceiver()).contains(session))
                || (request.getPayloadType() == PayloadType.START_TEST)
        );

    }


}
