package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Suggestion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SuggestionRepository extends MongoRepository<Suggestion, String> {
}
