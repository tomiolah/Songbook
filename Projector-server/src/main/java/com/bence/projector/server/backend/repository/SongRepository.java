package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Song;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface SongRepository extends CrudRepository<Song, Long> {
    List<Song> findAllByModifiedDateGreaterThan(Date modifiedDate);

    List<Song> findAllByVersionGroup(Song versionGroup);

    List<Song> findAllByYoutubeUrlNotNull();

    Song findOneByUuid(String uuid);
}
