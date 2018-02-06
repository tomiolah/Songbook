package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongVerse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongVerseRepository extends MongoRepository<SongVerse, String> {
}
