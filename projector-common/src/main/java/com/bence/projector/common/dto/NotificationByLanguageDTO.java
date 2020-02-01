package com.bence.projector.common.dto;

public class NotificationByLanguageDTO {
    private Boolean suggestions;
    private Boolean newSongs;
    private LanguageDTO language;

    public Boolean getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(Boolean suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean getNewSongs() {
        return newSongs;
    }

    public void setNewSongs(Boolean newSongs) {
        this.newSongs = newSongs;
    }

    public LanguageDTO getLanguage() {
        return language;
    }

    public void setLanguage(LanguageDTO language) {
        this.language = language;
    }
}
