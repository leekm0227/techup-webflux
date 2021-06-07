package com.example.demo.common;

import com.example.demo.v1.handler.ChannelWebSocketHandler;
import com.example.demo.v2.handler.ChannelWebSocketHandler2;
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
    final ChannelWebSocketHandler channelWebSocketHandler;
    final ChannelWebSocketHandler2 channelWebSocketHandler2;

    public SocketConfig(ChannelWebSocketHandler channelWebSocketHandler, ChannelWebSocketHandler2 channelWebSocketHandler2) {
        this.channelWebSocketHandler = channelWebSocketHandler;
        this.channelWebSocketHandler2 = channelWebSocketHandler2;
    }

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/v1/channel", channelWebSocketHandler);
        map.put("/v2/channel", channelWebSocketHandler2);
        return new SimpleUrlHandlerMapping(map, -1);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
