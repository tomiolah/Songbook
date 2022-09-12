package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.repository.SongVerseRepository;
import com.bence.projector.server.backend.repository.SuggestionRepository;
import com.bence.projector.server.backend.service.SuggestionService;
import com.bence.projector.server.utils.AppProperties;
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
    private final SongVerseRepository songVerseRepository;
    private final HashMap<String, Suggestion> suggestionHashMap;
    private long lastModifiedDateTime = 0;

    @Autowired
    public SuggestionServiceImpl(SuggestionRepository suggestionRepository, SongVerseRepository songVerseRepository) {
        this.suggestionRepository = suggestionRepository;
        this.songVerseRepository = songVerseRepository;
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
        Suggestion suggestion = suggestionRepository.findOneByUuid(id);
        if (AppProperties.getInstance().useMoreMemory()) {
            suggestionHashMap.put(id, suggestion);
        }
        return suggestion;
    }

    @Override
    public List<Suggestion> findAllByLanguage(Language language) {
        List<Suggestion> suggestionsByLanguage = suggestionRepository.findAllBySongLanguageId(language.getId());
        if (AppProperties.getInstance().useMoreMemory()) {
            return getSuggestionsFromMap(suggestionsByLanguage);
        }
        return suggestionsByLanguage;
    }

    private List<Suggestion> getSuggestionsFromMap(List<Suggestion> suggestionsByLanguage) {
        ArrayList<Suggestion> suggestions = new ArrayList<>();
        for (Suggestion suggestion : suggestionsByLanguage) {
            suggestions.add(findOneByUuid(suggestion.getUuid()));
        }
        return suggestions;
    }

    @Override
    public List<Suggestion> findAllBySong(Song song) {
        List<Suggestion> allBySongId = song.getSuggestions();
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
        if (AppProperties.getInstance().useMoreMemory()) {
            suggestionHashMap.put(suggestion.getUuid(), suggestion);
        }
        checkLastModifiedDate(suggestion);
    }

    @Override
    public Suggestion save(Suggestion suggestion) {
        List<SongVerse> verses = getCopyOfVerses(suggestion.getVerses());
        suggestionRepository.save(suggestion);
        songVerseRepository.deleteAllBySuggestionId(suggestion.getId());
        songVerseRepository.save(verses);
        return super.save(suggestion);
    }

    private List<SongVerse> getCopyOfVerses(List<SongVerse> verses) {
        if (verses == null) {
            return null;
        }
        return new ArrayList<>(verses);
    }

    @Override
    public Iterable<Suggestion> save(List<Suggestion> models) {
        for (Suggestion suggestion : models) {
            save(suggestion);
        }
        return models;
    }
}
