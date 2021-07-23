package com.example.demo.common;

import com.example.demo.handler.EventHandler1;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class WebSocketHandler1 implements WebSocketHandler {

    private final EventHandler1 eventHandler1;

    public WebSocketHandler1(EventHandler1 eventHandler1) {
        this.eventHandler1 = eventHandler1;
    }

    /*
        v1 blocking mongo template
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(session.receive().log("receive v1")
                .map(webSocketMessage -> eventHandler1.handle(webSocketMessage.getPayloadAsText()))
                .map(session::textMessage)
        );
    }
}
