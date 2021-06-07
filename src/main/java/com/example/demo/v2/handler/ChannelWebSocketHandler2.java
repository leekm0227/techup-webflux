package com.example.demo.v2.handler;

import com.example.demo.model.Payload;
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
    private final EventHandler eventHandler;
    private final BroadcastPublisher broadcastPublisher;

    public ChannelWebSocketHandler2(EventHandler eventHandler, BroadcastPublisher broadcastPublisher) {
        this.mapper = new ObjectMapper();
        this.eventHandler = eventHandler;
        this.broadcastPublisher = broadcastPublisher;
    }

    /*
        @ broadcast 방법 v2
        1. broadcastPublisher.map(filter: channelid or sessionid)
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(Flux.merge(
                broadcastPublisher.subscribe(session).map(payload -> session.textMessage(payloadToJson(payload))),
                session.receive().ofType(Request.class)
                        .doFirst(() -> broadcastPublisher.join(session))
                        .doFinally((signal) -> broadcastPublisher.leave(session))
                        .map(request -> session.textMessage(eventHandler.handle(request)))
        ));
    }

    private String payloadToJson(Payload payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
