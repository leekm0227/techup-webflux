package com.example.demo.v3.publisher.v2.handler;

import com.example.demo.model.Request;
import com.example.demo.v3.publisher.v2.publisher.BroadcastPublisher2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChannelWebSocketHandler3 implements WebSocketHandler {

    private final ObjectMapper mapper;
    private final EventHandler3 eventHandler3;
    private final BroadcastPublisher2 broadcastPublisher2;

    public ChannelWebSocketHandler3(EventHandler3 eventHandler3, BroadcastPublisher2 broadcastPublisher2) {
        this.mapper = new ObjectMapper();
        this.eventHandler3 = eventHandler3;
        this.broadcastPublisher2 = broadcastPublisher2;
    }

    /*
        @ broadcast 방법 v3
        1. 채널 생성시 pub 생성 및 merge.. 방법을 찾아보자
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> messages = Flux.merge(
                session.receive().log("receive v2")
                        .doFirst(() -> broadcastPublisher2.join(session))
                        .doFinally((signal) -> broadcastPublisher2.leave(session))
                        .map(webSocketMessage -> eventHandler3.handle(session.getId(), webSocketMessage.getPayloadAsText()))
                        .filter(s -> !s.isEmpty())
                        .map(session::textMessage)
        ).limitRequest(1L);

        return session.send(messages);
    }

    private String requestToJson(Request request) {
        try {
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
