package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.repository.SongCollectionRepository;
import com.bence.projector.server.backend.service.SongCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SongCollectionServiceImpl extends BaseServiceImpl<SongCollection> implements SongCollectionService {
    @Autowired
    private SongCollectionRepository songCollectionRepository;

    @Override
    public List<SongCollection> findAllByLanguageAndAndModifiedDateGreaterThan(Language language, Date lastModifiedDate) {
        return songCollectionRepository.findAllByLanguage_IdAndAndModifiedDateGreaterThan(language.getId(), lastModifiedDate);
    }

    @Override
    public boolean matches(SongCollection songCollection, SongCollection songCollection2) {
        if (!songCollection.getName().equals(songCollection2.getName())) {
            return false;
        }
        List<SongCollectionElement> songCollectionElements = songCollection.getSongCollectionElements();
        List<SongCollectionElement> songCollectionElements2 = songCollection2.getSongCollectionElements();
        if (songCollectionElements.size() != songCollectionElements2.size()) {
            return false;
        }
        for (int i = 0; i < songCollectionElements.size(); ++i) {
            if (!songCollectionElements.get(i).matches(songCollectionElements2.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<SongCollection> findAllBySong(Song song) {
        return songCollectionRepository.findAllBySongCollectionElements_SongId(song.getId());
    }

    @Override
    public SongCollection findOneByUuid(String uuid) {
        return songCollectionRepository.findOneByUuid(uuid);
    }

    @Override
    public List<SongCollection> findAll() {
        ArrayList<SongCollection> songCollections = new ArrayList<>();
        Iterable<SongCollection> all = songCollectionRepository.findAll();
        for (SongCollection songCollection : all) {
            if (!songCollection.isDeleted()) {
                songCollections.add(songCollection);
            }
        }
        return songCollections;
    }

    @Override
    public void deleteByUuid(String uuid) {

    }
}
