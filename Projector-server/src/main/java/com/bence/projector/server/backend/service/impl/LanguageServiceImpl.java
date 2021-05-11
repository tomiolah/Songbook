package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.repository.LanguageRepository;
import com.bence.projector.server.backend.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LanguageServiceImpl extends BaseServiceImpl<Language> implements LanguageService {
    private final Map<String, Language> languageMap = new HashMap<>();
    private final LanguageRepository languageRepository;

    @Autowired
    public LanguageServiceImpl(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Override
    public long countSongsById(String id) {
        Language language = languageRepository.findOneByUuid(id);
        if (language != null) {
            List<Song> songs = language.getSongs();
            if (songs != null) {
                return songs.size();
            }
        }
        return 0L;
    }

    @Override
    public void sortBySize(List<Language> languages) {
        for (Language language : languages) {
            language.setSongsCount(countSongsById(language.getUuid()));
        }
        languages.sort((o1, o2) -> Long.compare(o2.getSongsCount(), o1.getSongsCount()));
    }

    @Override
    public Language findLanguageBySongsContaining(Song song) {
        Language languageBySongsContaining = languageRepository.findLanguageBySongsContaining(song);
        if (languageBySongsContaining == null) {
            return null;
        }
        return findOneByUuid(languageBySongsContaining.getUuid());
    }

    @Override
    public List<Language> findAll() {
        Iterable<Language> languages = languageRepository.findAll();
        List<Language> allLanguages = new ArrayList<>();
        for (Language language : languages) {
            allLanguages.add(findOneByUuid(language.getUuid()));
        }
        return allLanguages;
    }

    @Override
    public void deleteByUuid(String uuid) {

    }

    @Override
    public Language findOneByUuid(String id) {
        if (languageMap.containsKey(id)) {
            return languageMap.get(id);
        }
        Language language = languageRepository.findOneByUuid(id);
        languageMap.put(id, language);
        return language;
    }
}
