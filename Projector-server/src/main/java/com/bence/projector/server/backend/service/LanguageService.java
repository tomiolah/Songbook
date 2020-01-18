package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;

import java.util.List;

public interface LanguageService extends BaseService<Language> {
    long countSongsById(String id);

    void sortBySize(List<Language> all);

    Language findLanguageBySongsContaining(Song song);
}
