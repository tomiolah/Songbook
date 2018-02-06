package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface SongCollectionRepository extends MongoRepository<SongCollection, String> {

    SongCollection findSongCollectionBySongCollectionElements_SongUuid(String songUuid);

    List<SongCollection> findAllByLanguage_IdAndAndModifiedDateGreaterThan(String language_id, Date modifiedDate);
}
