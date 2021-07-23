package com.example.demo.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class SocketConfig {
    final WebSocketHandler1 webSocketHandler1;
    final WebSocketHandler2 webSocketHandler2;

    public SocketConfig(WebSocketHandler1 webSocketHandler1, WebSocketHandler2 webSocketHandler2) {
        this.webSocketHandler1 = webSocketHandler1;
        this.webSocketHandler2 = webSocketHandler2;
    }

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/v1/channel", webSocketHandler1);
        map.put("/v2/channel", webSocketHandler2);
        return new SimpleUrlHandlerMapping(map, -1);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
