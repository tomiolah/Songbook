package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongLink;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongLinkRepository extends MongoRepository<SongLink, String> {
}
