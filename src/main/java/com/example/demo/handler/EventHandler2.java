package com.example.demo.handler;

import com.example.demo.model.PayloadType;
import com.example.demo.model.Request;
import com.example.demo.model.Test;
import com.example.demo.repository.TestRepository2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class EventHandler2 {

    private final TestRepository2 repository;

    public EventHandler2(TestRepository2 repository) {
        this.repository = repository;
    }

    public Flux<Test> handle(Request request) {
        Test test = new Test() {{
            setRegTime(request.getRegTime());
        }};

        if (request.getPayloadType() == PayloadType.TEST) {
            test.setPayloadType(PayloadType.TEST);
            return Flux.just(test);
        } else {
            test.setPayloadType(PayloadType.FIND_TEST);
            return repository.findByRare(request.getCode()).defaultIfEmpty(test);
        }
    }
}
