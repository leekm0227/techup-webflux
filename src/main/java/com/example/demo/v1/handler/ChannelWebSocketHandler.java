package com.example.demo.v1.handler;

import com.example.demo.v1.manager.SessionManager;
import com.example.demo.model.Request;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class ChannelWebSocketHandler implements WebSocketHandler {

    private final EventHandler eventHandler;
    private final SessionManager sessionManager;

    public ChannelWebSocketHandler(EventHandler eventHandler, SessionManager sessionManager) {
        this.eventHandler = eventHandler;
        this.sessionManager = sessionManager;
    }

    /*
        @ broadcast 방법 v1
        manager를 통하여 해당 session or channel에 flux.just로 메시지 전송
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(session.receive().ofType(Request.class)
                .doFirst(() -> sessionManager.add(session))
                .doFinally((signal) -> sessionManager.remove(session))
                .map(request -> session.textMessage(eventHandler.handle(request)))
        );
    }
}
