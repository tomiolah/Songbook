package com.bence.songbook.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.List;

public class Language extends BaseEntity {

    @DatabaseField
    private String englishName;
    @DatabaseField
    private String nativeName;
    @DatabaseField
    private boolean selected;
    @ForeignCollectionField
    private ForeignCollection<Song> songForeignCollection;
    private List<Song> songs;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @SuppressWarnings("ConstantConditions")
    public List<Song> getSongs() {
        if (songs == null) {
            if (songForeignCollection == null) {
                return new ArrayList<>();
            }
            List<Song> songs = new ArrayList<>(songForeignCollection.size());
            songs.addAll(songForeignCollection);
            this.songs = songs;
            return songs;
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        for (Song song : songs) {
            song.setLanguage(this);
        }
        this.songs = songs;
    }
}
