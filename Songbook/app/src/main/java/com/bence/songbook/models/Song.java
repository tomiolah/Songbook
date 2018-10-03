package com.bence.songbook.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.songbook.ui.utils.StringUtils.stripAccents;

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
    private Date nullDate;
    private String createdByEmail;
    @DatabaseField
    private String versionGroup;
    @DatabaseField
    private String youtubeUrl;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private FavouriteSong favourite;

    public Song() {
    }

    public static void copyLocallySetted(Song song, Song modifiedSong) {
        song.setFavourite(modifiedSong.getFavourite());
        song.setAccessedTimeAverage(modifiedSong.getAccessedTimeAverage());
        song.setAccessedTimes(modifiedSong.getAccessedTimes());
        song.setLastAccessed(modifiedSong.getLastAccessed());
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

    private Date getNullDate() {
        if (nullDate == null) {
            nullDate = new Date(0);
        }
        return nullDate;
    }

    public Date getLastAccessed() {
        if (lastAccessed == null) {
            return getNullDate();
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

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public boolean isFavourite() {
        return favourite != null && favourite.isFavourite();
    }

    public void setFavourite(boolean favourite) {
        if (this.favourite == null) {
            this.favourite = new FavouriteSong();
            this.favourite.setSong(this);
        }
        this.favourite.setFavourite(favourite);
        this.favourite.setModifiedDate(new Date());
        this.favourite.setFavouritePublishedToDrive(false);
    }

    public FavouriteSong getFavourite() {
        return favourite;
    }

    public void setFavourite(FavouriteSong favouriteSong) {
        this.favourite = favouriteSong;
        favouriteSong.setSong(this);
    }

    @Override
    public String toString() {
        return title;
    }
}
