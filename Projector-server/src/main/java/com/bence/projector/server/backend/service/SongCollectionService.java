package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.SongCollection;

import java.util.Date;
import java.util.List;

public interface SongCollectionService extends BaseService<SongCollection> {
    SongCollection findSongCollectionBySongCollectionElements_SongUuid(String songUuid);

    List<SongCollection> findAllByLanguage_IdAndAndModifiedDateGreaterThan(String language_id, Date lastModifiedDate);
}
