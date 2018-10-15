package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongList;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SongListRepository extends MongoRepository<SongList, String> {
}
