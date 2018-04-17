package com.bence.songbook.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.songbook.utils.StringUtils.stripAccents;

public class Song extends BaseEntity {

    @DatabaseField
    private String title;
    @DatabaseField
    private String strippedTitle;
    @ForeignCollectionField
    private ForeignCollection<SongVerse> songVerseForeignCollection;
    private List<SongVerse> verses;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    private boolean deleted = false;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    @DatabaseField
    private Date lastAccessed;
    @DatabaseField
    private Long accessedTimes;
    @DatabaseField
    private Long accessedTimeAverage;
    private SongCollection songCollection;
    private SongCollectionElement songCollectionElement;
    private Date nullDate = new Date(0);
    private String createdByEmail;
    @DatabaseField
    private String versionGroup;

    public Song() {
    }

    public List<SongVerse> getClonedVerses() {
        ArrayList<SongVerse> clonedSongVerses = new ArrayList<>(verses.size());
        for (SongVerse songVerse : verses) {
            clonedSongVerses.add(new SongVerse(songVerse));
        }
        return clonedSongVerses;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        strippedTitle = stripAccents(title.toLowerCase());
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
    }

    public ForeignCollection<SongVerse> getForeignCollectionVerses() {
        return songVerseForeignCollection;
    }

    public List<SongVerse> getVerses() {
        if (verses == null) {
            List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
            songVerses.addAll(songVerseForeignCollection);
            verses = songVerses;
            return songVerses;
        }
        return verses;
    }

    public void setVerses(List<SongVerse> verseList) {
        for (SongVerse songVerse : verseList) {
            songVerse.setSong(this);
        }
        this.verses = verseList;
    }

    public void fetchVerses() {
        if (verses == null) {
            List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
            songVerses.addAll(songVerseForeignCollection);
            verses = songVerses;
        }
    }

    public String getStrippedTitle() {
        return strippedTitle;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Date getLastAccessed() {
        if (lastAccessed == null) {
            return nullDate;
        }
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Long getAccessedTimes() {
        if (accessedTimes == null) {
            accessedTimes = 0L;
        }
        return accessedTimes;
    }

    public void setAccessedTimes(Long accessedTimes) {
        this.accessedTimes = accessedTimes;
    }

    public Long getAccessedTimeAverage() {
        if (accessedTimeAverage == null) {
            accessedTimeAverage = 0L;
        }
        return accessedTimeAverage;
    }

    public void setAccessedTimeAverage(Long accessedTimeAverage) {
        this.accessedTimeAverage = accessedTimeAverage;
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    public void setSongCollection(SongCollection songCollection) {
        this.songCollection = songCollection;
    }

    public SongCollectionElement getSongCollectionElement() {
        return songCollectionElement;
    }

    public void setSongCollectionElement(SongCollectionElement songCollectionElement) {
        this.songCollectionElement = songCollectionElement;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }
}
