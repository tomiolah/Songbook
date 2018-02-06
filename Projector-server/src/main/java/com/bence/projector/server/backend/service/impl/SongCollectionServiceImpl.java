package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.repository.SongCollectionRepository;
import com.bence.projector.server.backend.service.SongCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SongCollectionServiceImpl extends BaseServiceImpl<SongCollection> implements SongCollectionService {
    @Autowired
    private SongCollectionRepository songCollectionRepository;

    @Override
    public SongCollection findSongCollectionBySongCollectionElements_SongUuid(String songUuid) {
        return songCollectionRepository.findSongCollectionBySongCollectionElements_SongUuid(songUuid);
    }

    @Override
    public List<SongCollection> findAllByLanguage_IdAndAndModifiedDateGreaterThan(String language_id, Date lastModifiedDate) {
        return songCollectionRepository.findAllByLanguage_IdAndAndModifiedDateGreaterThan(language_id, lastModifiedDate);
    }
}
