package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Bible;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BibleRepository extends MongoRepository<Bible, String> {
}
