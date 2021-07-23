package com.example.demo.repository;

import com.example.demo.model.Test;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TestRepository2 extends ReactiveMongoRepository<Test, String> {
    Flux<Test> findByRare(int code);
}
