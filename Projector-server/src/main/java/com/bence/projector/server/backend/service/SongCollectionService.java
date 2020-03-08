package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;

import java.util.Date;
import java.util.List;

public interface SongCollectionService extends BaseService<SongCollection> {
    SongCollection findSongCollectionBySongCollectionElements_SongUuid(String songUuid);

    List<SongCollection> findAllByLanguage_IdAndAndModifiedDateGreaterThan(String language_id, Date lastModifiedDate);

    boolean matches(SongCollection savedSongCollection, SongCollection songCollection1);

    List<SongCollection> findAllBySong(Song song);

    List<SongCollectionElement> findSongCollectionElementsBySong(Song song);
}
