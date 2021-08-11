package com.example.demo.publisher;

import com.example.demo.model.Request;
import com.example.demo.model.type.ReceiveType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Sinks;

@Component
public class BroadcastPublisher {
    private final Sinks.Many<Request> sink;

    public BroadcastPublisher() {
        this.sink = Sinks.many().replay().limit(0);
    }

    public void next(Request request) {
        sink.tryEmitNext(request);
    }

    public Flux<Request> subscribe() {
        return sink.asFlux().log("broadcast");
    }
}
