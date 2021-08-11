package com.example.demo.handler;

import com.example.demo.manager.PlayerManager;
import com.example.demo.model.Request;
import com.example.demo.publisher.BroadcastPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChannelWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper mapper;
    private final EventHandler eventHandler;
    private final BroadcastPublisher broadcastPublisher;
    private final PlayerManager playerManager;

    public ChannelWebSocketHandler(EventHandler eventHandler, BroadcastPublisher broadcastPublisher, PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.mapper = new ObjectMapper();
        this.eventHandler = eventHandler;
        this.broadcastPublisher = broadcastPublisher;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(Flux.merge(
                broadcastPublisher.subscribe().map(payload -> session.textMessage(requestToJson(payload))),
                session.receive().log("recv")
                        .doFinally((signal) -> playerManager.dead(session.getId()))
                        .map(webSocketMessage -> eventHandler.handle(session.getId(), webSocketMessage.getPayloadAsText()))
                        .filter(s -> !s.isEmpty())
                        .map(session::textMessage)
        ));
    }

    private String requestToJson(Request request) {
        try {
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
