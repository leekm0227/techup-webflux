package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
public class DemoWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    EnemyPublisher enemyPublisher;

    @Autowired
    ScorePublisher scorePublisher;

    @Autowired
    ScoreManager scoreManager;

    @Autowired
    EnemyManager enemyManager;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        session.receive().map(WebSocketMessage::getPayloadAsText)
                .doFirst(() -> scoreManager.join(session.getId()))
                .doFinally(signalType -> scoreManager.leave(session.getId()))
                .subscribe(s -> enemyManager.next(session.getId(), s));

        return session.send(Mono.just(this.name(scoreManager.getName(session.getId()))).map(session::textMessage)
                .mergeWith(enemyPublisher.subscribe().map(this::enemyToJson).map(session::textMessage))
                .mergeWith(scorePublisher.subscribe().map(this::scoreToJson).map(session::textMessage)));
    }

    private String name(String name) {
        try {
            HashMap<String, String> nameMap = new HashMap<>();
            nameMap.put("name", name);
            return mapper.writeValueAsString(nameMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String enemyToJson(HashMap<String, Long> out) {
        try {
            HashMap<String, HashMap<String, Long>> enemy = new HashMap<>();
            enemy.put("enemies", out);
            return mapper.writeValueAsString(enemy);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String scoreToJson(HashMap<String, Integer> out) {
        try {
            HashMap<String, HashMap<String, Integer>> scores = new HashMap<>();
            scores.put("scores", out);
            return mapper.writeValueAsString(scores);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
