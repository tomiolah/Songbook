package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Stack;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StackRepository extends MongoRepository<Stack, String> {
    Stack findByStackTrace(String stackTrace);
}
