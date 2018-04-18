package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Song;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface SongRepository extends MongoRepository<Song, String> {
    List<Song> findAllByModifiedDateGreaterThan(Date modifiedDate);

    List<Song> findAllByUploadedTrueAndDeletedTrue();

    List<Song> findAllByVersionGroup(String versionGroup);
}
