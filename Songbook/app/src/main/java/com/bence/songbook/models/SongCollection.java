package com.bence.songbook.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongCollection extends BaseEntity {
    @ForeignCollectionField
    private ForeignCollection<SongCollectionElement> songCollectionElementForeignCollection;
    private List<SongCollectionElement> songCollectionElements;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private String name;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    private boolean selected;

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            //noinspection ConstantConditions
            if (songCollectionElementForeignCollection == null) {
                songCollectionElements = new ArrayList<>();
                return songCollectionElements;
            }
            songCollectionElements = new ArrayList<>(songCollectionElementForeignCollection.size());
            songCollectionElements.addAll(songCollectionElementForeignCollection);
        }
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElement> songCollectionElements) {
        this.songCollectionElements = songCollectionElements;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
        if (modifiedDate == null) {
            this.modifiedDate = new Date(0);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
