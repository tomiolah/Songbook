package com.bence.projector.server.backend.model;

import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongCollection extends BaseEntity {
    private List<SongCollectionElement> songCollectionElements;
    private Date createdDate;
    private Date modifiedDate;
    private String name;
    @DBRef
    private Language language;
    private String originalId;
    private Boolean deleted;
    private Boolean uploaded;

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            songCollectionElements = new ArrayList<>();
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

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public boolean isDeleted() {
        return deleted == null ? false : deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isUploaded() {
        return uploaded == null ? false : uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }
}
