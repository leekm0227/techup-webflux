package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class SocketConfig {

    @Autowired
    EnemyManager enemyManager;

    @Autowired
    ScoreManager scoreManager;

    @Autowired
    DemoWebSocketHandler demoWebSocketHandler;

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/demo", demoWebSocketHandler);
        return new SimpleUrlHandlerMapping(map, -1);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route()
                .GET("/debug", request -> ServerResponse.ok().body(BodyInserters.fromResource(new ClassPathResource("debug.html"))))
                .GET("/", request -> ServerResponse.ok().body(BodyInserters.fromResource(new ClassPathResource("index.html"))))
                .build();
    }
}
