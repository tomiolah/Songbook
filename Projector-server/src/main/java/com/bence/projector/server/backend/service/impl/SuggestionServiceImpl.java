package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.repository.SuggestionRepository;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class SuggestionServiceImpl extends BaseServiceImpl<Suggestion> implements SuggestionService {
    private final SuggestionRepository suggestionRepository;
    private final SongService songService;
    private final HashMap<String, Suggestion> suggestionHashMap;
    private long lastModifiedDateTime = 0;

    @Autowired
    public SuggestionServiceImpl(SuggestionRepository suggestionRepository, SongService songService) {
        this.suggestionRepository = suggestionRepository;
        this.songService = songService;
        suggestionHashMap = new HashMap<>(500);
    }

    @Override
    public List<Suggestion> findAll() {
        List<Suggestion> suggestions = new ArrayList<>(suggestionHashMap.size());
        suggestions.addAll(getSuggestions());
        return suggestions;
    }

    @Override
    public Suggestion findOneByUuid(String id) {
        if (suggestionHashMap.containsKey(id)) {
            return suggestionHashMap.get(id);
        }
        Suggestion suggestion = super.findOneByUuid(id);
        suggestionHashMap.put(id, suggestion);
        return suggestion;
    }

    @Override
    public List<Suggestion> findAllByLanguage(Language language) {
        String languageId = language.getUuid();
        List<Suggestion> suggestions = new ArrayList<>(suggestionHashMap.size());
        for (Suggestion suggestion : getSuggestions()) {
            try {
                Song song1 = suggestion.getSong();
                if (song1 == null) {
                    System.out.println("suggestion has null song1: " + suggestion.getId());
                    continue;
                }
                Song song = songService.findOneByUuid(song1.getUuid());
                String id = song.getLanguage().getUuid();
                if (id.equals(languageId)) {
                    suggestions.add(suggestion);
                }
            } catch (NullPointerException ignored) {
            }
        }
        return suggestions;
    }

    @Override
    public List<Suggestion> findAllBySong(Song song) {
        List<Suggestion> allBySongId = suggestionRepository.findAllBySongId(song.getUuid());
        List<Suggestion> suggestions = new ArrayList<>(allBySongId.size());
        for (Suggestion suggestion : allBySongId) {
            suggestions.add(findOneByUuid(suggestion.getUuid()));
        }
        return suggestions;
    }

    private Collection<Suggestion> getSuggestions() {
        if (suggestionHashMap.isEmpty()) {
            for (Suggestion suggestion : suggestionRepository.findAll()) {
                putInMapAndCheckLastModifiedDate(suggestion);
            }
        } else {
            for (Suggestion suggestion : suggestionRepository.findAllByModifiedDateGreaterThan(new Date(lastModifiedDateTime))) {
                if (!suggestionHashMap.containsKey(suggestion.getUuid())) {
                    putInMapAndCheckLastModifiedDate(suggestion);
                } else {
                    suggestionHashMap.replace(suggestion.getUuid(), suggestion);
                    checkLastModifiedDate(suggestion);
                }
            }
        }
        return suggestionHashMap.values();
    }

    private void checkLastModifiedDate(Suggestion suggestion) {
        Date modifiedDate = suggestion.getModifiedDate();
        if (modifiedDate == null) {
            return;
        }
        long time = modifiedDate.getTime();
        if (time > lastModifiedDateTime) {
            lastModifiedDateTime = time;
        }
    }

    private void putInMapAndCheckLastModifiedDate(Suggestion suggestion) {
        suggestionHashMap.put(suggestion.getUuid(), suggestion);
        checkLastModifiedDate(suggestion);

    }
}
