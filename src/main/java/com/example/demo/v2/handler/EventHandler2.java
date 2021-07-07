package com.example.demo.v2.handler;

import com.example.demo.model.PayloadType;
import com.example.demo.model.Request;
import com.example.demo.model.ResultType;
import com.example.demo.v2.publisher.BroadcastPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class EventHandler2 {

    private static final HashMap<PayloadType, Function<Request, String>> eventMap = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final BroadcastPublisher broadcastPublisher;

    EventHandler2(BroadcastPublisher broadcastPublisher) {
        this.broadcastPublisher = broadcastPublisher;
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
            result.putIfAbsent("result", ResultType.SUCCESS);
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private Request parse(String payloadText) {
        try {
            return mapper.readValue(payloadText, Request.class);
        } catch (JsonProcessingException e) {
            return new Request();
        }
    }

    public String handle(String sessionId, String payloadText) {
        Request request = parse(payloadText);
        request.setSessionId(sessionId);
        return eventMap.getOrDefault(request.getPayloadType(), this::invalid).apply(request);
    }

    private String invalid(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("result", ResultType.ERROR_INVALID_PAYLOAD_TYPE);
        return response(result);
    }

    private String channelList(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("list", broadcastPublisher.getList());
        return response(result);
    }

    private String channelCreate(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("channelId", broadcastPublisher.createChannel(request.getSessionId()));
        return response(result);
    }

    private String channelJoin(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        broadcastPublisher.joinChannel(request.getChannelId(), request.getSessionId());
        return response(result);
    }

    private String channelLeave(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        broadcastPublisher.leaveChannel(request.getChannelId(), request.getSessionId());
        return response(result);
    }

    private String broadcast(Request request) {
        broadcastPublisher.next(request);
        return "";
    }
}