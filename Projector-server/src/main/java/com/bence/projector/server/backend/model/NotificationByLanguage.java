package com.bence.projector.server.backend.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationByLanguage {

    private static final int INITIAL_DELAY = 15 * 60 * 1000;
    @DBRef(lazy = true)
    private Language language;
    private Boolean suggestions;
    private Boolean newSongs;
    private Integer suggestionsDelay;
    private Integer newSongsDelay;
    private Date suggestionsLastSentDate;
    private Date newSongsLastSentDate;
    @DBRef(lazy = true)
    private List<Suggestion> suggestionStack;
    @DBRef(lazy = true)
    private List<Song> newSongStack;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Boolean isSuggestions() {
        return suggestions != null && suggestions;
    }

    public void setSuggestions(Boolean suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean isNewSongs() {
        return newSongs != null && newSongs;
    }

    public void setNewSongs(Boolean newSongs) {
        this.newSongs = newSongs;
    }

    public Integer getSuggestionsDelay() {
        if (suggestionsDelay == null) {
            suggestionsDelay = INITIAL_DELAY;
        }
        return suggestionsDelay;
    }

    public void setSuggestionsDelay(Integer suggestionsDelay) {
        this.suggestionsDelay = suggestionsDelay;
    }

    public Integer getNewSongsDelay() {
        if (newSongsDelay == null) {
            newSongsDelay = INITIAL_DELAY;
        }
        return newSongsDelay;
    }

    public void setNewSongsDelay(Integer newSongsDelay) {
        this.newSongsDelay = newSongsDelay;
    }

    public Date getSuggestionsLastSentDate() {
        if (suggestionsLastSentDate == null) {
            return new Date(0);
        }
        return suggestionsLastSentDate;
    }

    public void setSuggestionsLastSentDate(Date suggestionsLastSentDate) {
        this.suggestionsLastSentDate = suggestionsLastSentDate;
    }

    public Date getNewSongsLastSentDate() {
        if (newSongsLastSentDate == null) {
            return new Date(0);
        }
        return newSongsLastSentDate;
    }

    public void setNewSongsLastSentDate(Date newSongsLastSentDate) {
        this.newSongsLastSentDate = newSongsLastSentDate;
    }

    public List<Suggestion> getSuggestionStack() {
        if (suggestionStack == null) {
            suggestionStack = new ArrayList<>();
        }
        return suggestionStack;
    }

    public void setSuggestionStack(List<Suggestion> suggestionStack) {
        this.suggestionStack = suggestionStack;
    }

    public List<Song> getNewSongStack() {
        if (newSongStack == null) {
            newSongStack = new ArrayList<>();
        }
        return newSongStack;
    }

    public void setNewSongStack(List<Song> newSongStack) {
        this.newSongStack = newSongStack;
    }
}
