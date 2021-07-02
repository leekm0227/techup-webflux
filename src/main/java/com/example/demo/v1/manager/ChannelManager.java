package com.example.demo.v1.manager;

import com.example.demo.model.PayloadType;
import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelManager {

    private final ConcurrentHashMap<String, List<Sinks.Many<Request>>> channelMap = new ConcurrentHashMap<>();
    private final SessionManager sessionManager;
    private List<Sinks.Many<Request>> sessionList = null;

    public ChannelManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        for (int i = 0; i < 50; i++) {
            channelMap.put("channel" + i, new ArrayList<>());
        }
    }

    public String createChannel(String sessionId) {
        String channelId = Integer.toString(ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()).getInt(), 9);
        List<Sinks.Many<Request>> sessions = new ArrayList<>();
        sessions.add(sessionManager.getBySessionId(sessionId));
        channelMap.put(channelId, sessions);
        return channelId;
    }

    public void ready() {
        if (sessionManager.getList().size() >= 1000) {
            if (this.sessionList == null) {
                this.sessionList = sessionManager.getList();

                for (Sinks.Many<Request> session : sessionList) {
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
                        this.broadcast(request);
                    }
                }
            }
        }
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
