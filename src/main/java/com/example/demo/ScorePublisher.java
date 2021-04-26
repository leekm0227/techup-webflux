package com.example.demo;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class ScorePublisher {
    private Sinks.Many<HashMap<String, Integer>> scoreSink;

    @PostConstruct
    void init() {
        this.scoreSink = Sinks.many().replay().limit(1);
    }

    public void next(HashMap<String, Integer> score) {
        scoreSink.tryEmitNext(score);
    }

    public Flux<HashMap<String, Integer>> subscribe() {
        return scoreSink.asFlux();
    }
}
