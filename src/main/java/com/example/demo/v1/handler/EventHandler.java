package com.example.demo.v1.handler;

import com.example.demo.v1.manager.ChannelManager;
import com.example.demo.v1.manager.SessionManager;
import com.example.demo.model.PayloadType;
import com.example.demo.model.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class EventHandler {

    private static final HashMap<PayloadType, Function<Request, String>> eventMap = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChannelManager channelManager;
    private final SessionManager sessionManager;

    EventHandler(ChannelManager channelManager, SessionManager sessionManager) {
        this.channelManager = channelManager;
        this.sessionManager = sessionManager;
    }

    @PostConstruct
    void init() {
        eventMap.put(PayloadType.CHANNEL_LIST, this::channelList);
        eventMap.put(PayloadType.CHANNEL_CREATE, this::channelCreate);
        eventMap.put(PayloadType.CHANNEL_JOIN, this::channelJoin);
        eventMap.put(PayloadType.CHANNEL_LEAVE, this::channelLeave);
        eventMap.put(PayloadType.BROADCAST, this::broadcast);
    }

    private String response(HashMap<String, Object> result) {
        try {
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public String handle(Request request) {
        return eventMap.getOrDefault(request.getPayloadType(), this::invalid).apply(request);
    }

    private String invalid(Request request) {
        System.out.println("invalid payload type");
        return response(null);
    }

    private String channelList(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("list", channelManager.getList());
        return response(result);
    }

    private String channelCreate(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("channelId", channelManager.createChannel(request.getPayload().getSessionId()));
        return response(result);
    }

    private String channelJoin(Request request) {
        channelManager.joinChannel(request.getPayload().getChannelId(), request.getPayload().getSessionId());
        return response(null);
    }

    private String channelLeave(Request request) {
        channelManager.leaveChannel(request.getPayload().getChannelId(), request.getPayload().getSessionId());
        return response(null);
    }

    private String broadcast(Request request) {
        switch (request.getPayload().getReceiveType()) {
            case SESSION:
                sessionManager.broadcast(request);
                break;
            case CHANNEL:
                channelManager.broadcast(request);
                break;
        }

        return response(null);
    }
}
