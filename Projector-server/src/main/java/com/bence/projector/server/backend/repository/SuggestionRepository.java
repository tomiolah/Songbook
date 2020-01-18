package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Suggestion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface SuggestionRepository extends MongoRepository<Suggestion, String> {
    List<Suggestion> findAllByModifiedDateGreaterThan(Date createdDate);
}
