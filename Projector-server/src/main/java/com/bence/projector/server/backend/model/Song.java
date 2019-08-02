package com.bence.projector.server.backend.model;

import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.List;

public class Song extends BaseEntity {

    private String originalId;
    private String title;
    private List<SongVerse> verses;
    private Date createdDate;
    private Date modifiedDate;
    private boolean deleted = false;
    @Transient
    private Language language;
    private Boolean uploaded;
    private long views;
    private Date lastIncrementViewDate;
    private long favourites;
    private Date lastIncrementFavouritesDate;
    private String createdByEmail;
    @Transient
    transient private double percentage;
    private String versionGroup;
    private String youtubeUrl;
    private String verseOrder;
    private String author;

    public Song() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SongVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<SongVerse> verses) {
        this.verses = verses;
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

    @Override
    public String toString() {
        return title;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void incrementViews() {
        ++this.views;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public Date getLastIncrementViewDate() {
        return lastIncrementViewDate;
    }

    public void setLastIncrementViewDate(Date lastIncrementViewDate) {
        this.lastIncrementViewDate = lastIncrementViewDate;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public boolean isUploaded() {
        return uploaded != null && uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public Date getLastIncrementFavouritesDate() {
        return lastIncrementFavouritesDate;
    }

    public void setLastIncrementFavouritesDate(Date lastIncrementFavouritesDate) {
        this.lastIncrementFavouritesDate = lastIncrementFavouritesDate;
    }

    public void incrementFavourites() {
        ++favourites;
    }

    public long getFavourites() {
        return favourites;
    }

    public void setFavourites(long favourites) {
        this.favourites = favourites;
    }

    public String getVerseOrder() {
        return verseOrder;
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = verseOrder;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
