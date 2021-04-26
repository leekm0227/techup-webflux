package com.example.demo;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class EnemyPublisher {
    private Sinks.Many<HashMap<String, Long>> enemySink;

    @PostConstruct
    void init() {
        this.enemySink = Sinks.many().replay().limit(1);
    }

    public void next(HashMap<String, Long> enemy) {
        enemySink.tryEmitNext(enemy);
    }

    public Flux<HashMap<String, Long>> subscribe() {
        return enemySink.asFlux();
    }
}
