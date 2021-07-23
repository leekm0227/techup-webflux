package com.example.demo.common;

import com.example.demo.handler.EventHandler2;
import com.example.demo.model.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class WebSocketHandler2 implements WebSocketHandler {

    private final EventHandler2 eventHandler2;
    private final ObjectMapper mapper;

    public WebSocketHandler2(EventHandler2 eventHandler2) {
        this.eventHandler2 = eventHandler2;
        this.mapper = new ObjectMapper();
    }

    /*
        v2 non-blocking mongodb
    */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(session.receive().log("receive v2")
                .map(message -> messageToRequest(message.getPayloadAsText()))
                .flatMap(eventHandler2::handle)
                .map(test -> session.textMessage(objToString(test)))
        );
    }

    private Request messageToRequest(String paylaodText) {
        try {
            return mapper.readValue(paylaodText, Request.class);
        } catch (JsonProcessingException e) {
            return new Request();
        }
    }

    private String objToString(Object obj) {
        try {
            return (mapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            return "test";
        }
    }
}
