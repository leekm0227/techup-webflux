package com.example.demo.v1.handler;

import com.example.demo.v1.manager.ChannelManager;
import com.example.demo.v1.manager.SessionManager;
import com.example.demo.model.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class ChannelWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper mapper;
    private final EventHandler eventHandler;
    private final SessionManager sessionManager;
    private final ChannelManager channelManager;

    public ChannelWebSocketHandler(EventHandler eventHandler, SessionManager sessionManager, ChannelManager channelManager) {
        this.mapper = new ObjectMapper();
        this.eventHandler = eventHandler;
        this.sessionManager = sessionManager;
        this.channelManager = channelManager;
    }

    /*
        @ broadcast 방법 v1
        sessionManager를 통하여 해당 session에 subscriber 생성, 메시지 전송

        -> channelMap.foreach시 blocking 발생
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Sinks.Many<Request> subscriber = Sinks.many().unicast().onBackpressureError();
        return session.send(subscriber.asFlux().map(request -> session.textMessage(requestToJson(request)))
                .mergeWith(session.receive().log("receive v1")
                        .doFirst(() -> {
                            sessionManager.add(session.getId(), subscriber);
                            channelManager.ready();
                        })
                        .doFinally((signal) -> sessionManager.remove(session))
                        .map(webSocketMessage -> eventHandler.handle(session.getId(), webSocketMessage.getPayloadAsText()))
                        .filter(s -> !s.isEmpty())
                        .map(session::textMessage)
                )
        );
    }

    private String requestToJson(Request request) {
        try {
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
