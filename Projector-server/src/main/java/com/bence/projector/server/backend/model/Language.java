package com.bence.projector.server.backend.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

public class Language extends BaseEntity {
    private String englishName;
    private String nativeName;
    @DBRef(lazy = true)
    private List<Song> songs;
    private long songsCount;

    public Language() {
    }

    public void setLanguageForSongs() {
        for (Song song : getSongs()) {
            song.setLanguage(this);
        }
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public List<Song> getSongs() {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Language other = (Language) obj;
        if (this.getId() == null) {
            return other.getId() == null;
        } else {
            return this.getId().equals(other.getId());
        }
    }

    public long getSongsCount() {
        return songsCount;
    }

    public void setSongsCount(long songsCount) {
        this.songsCount = songsCount;
    }
}
