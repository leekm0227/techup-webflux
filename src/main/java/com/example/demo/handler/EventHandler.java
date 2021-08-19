package com.example.demo.handler;

import com.example.demo.manager.PlayerManager;
import com.example.demo.model.Player;
import com.example.demo.model.Request;
import com.example.demo.model.type.PayloadType;
import com.example.demo.model.type.ResultType;
import com.example.demo.publisher.BroadcastPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.function.Function;

@Component
public class EventHandler {

    private final HashMap<PayloadType, Function<Request, String>> eventMap;
    private final ObjectMapper mapper;
    private final BroadcastPublisher broadcastPublisher;
    private final PlayerManager playerManager;

    EventHandler(BroadcastPublisher broadcastPublisher, PlayerManager playerManager) {
        this.broadcastPublisher = broadcastPublisher;
        this.playerManager = playerManager;
        this.mapper = new ObjectMapper();
        this.eventMap = new HashMap<>();

        eventMap.put(PayloadType.INIT, this::init);
        eventMap.put(PayloadType.MOVE, this::move);
        eventMap.put(PayloadType.ATTACK, this::attack);
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

    private String init(Request request) {
        HashMap<String, Object> result = new HashMap<>();
        playerManager.spawn(request.getSessionId());
        result.put("payloadType", PayloadType.INIT);
        result.put("id", request.getSessionId());
        result.put("players", playerManager.list());
        return response(result);
    }

    private String move(Request request) {
        Integer[] pos = playerManager.move(request.getSessionId(), request.getDir());
        if (pos == null) return "";
        request.setPlayer(new Player() {{
            setId(request.getSessionId());
            setPos(pos);
        }});
        broadcastPublisher.next(request);
        return "";
    }

    private String attack(Request request) {
        if (request.getTargetId().equals("") || request.getSessionId().equals(request.getTargetId())) return "";
        Integer[] target = playerManager.attack(request.getSessionId(), request.getTargetId());
        if (target == null) return "";
        if (target[0] < 1) {
            playerManager.dead(request.getTargetId());
            return "";
        }

        request.setPlayer(new Player() {{
            setId(request.getSessionId());
            setHp(target);
        }});
        broadcastPublisher.next(request);
        return "";
    }
}
