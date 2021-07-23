package com.example.demo.repository;

import com.example.demo.model.Test;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestRepository1 extends MongoRepository<Test, String> {
    Optional<Test> findByRare(int code);
}
