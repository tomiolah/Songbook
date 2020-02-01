package com.bence.projector.server.backend.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

public class NotificationByLanguage {

    @DBRef(lazy = true)
    private Language language;
    private Boolean suggestions;
    private Boolean newSongs;

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
}
