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
    private String createdByEmail;
    @Transient
    transient private double percentage;
    private String versionGroup;

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

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
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
}
