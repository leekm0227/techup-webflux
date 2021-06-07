package com.example.demo.v2.handler;

import com.example.demo.model.Request;
import com.example.demo.v2.publisher.BroadcastPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChannelWebSocketHandler2 implements WebSocketHandler {

    private final ObjectMapper mapper;
    private final EventHandler2 eventHandler2;
    private final BroadcastPublisher broadcastPublisher;

    public ChannelWebSocketHandler2(EventHandler2 eventHandler2, BroadcastPublisher broadcastPublisher) {
        this.mapper = new ObjectMapper();
        this.eventHandler2 = eventHandler2;
        this.broadcastPublisher = broadcastPublisher;
    }

    /*
        @ broadcast 방법 v2
        1. broadcastPublisher.filter(channelid or sessionid)
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(Flux.merge(
                broadcastPublisher.subscribe(session).map(payload -> session.textMessage(requestToJson(payload))),
                session.receive().log("receive v2")
                        .doFirst(() -> broadcastPublisher.join(session))
                        .doFinally((signal) -> broadcastPublisher.leave(session))
                        .map(webSocketMessage -> eventHandler2.handle(session.getId(), webSocketMessage.getPayloadAsText()))
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
