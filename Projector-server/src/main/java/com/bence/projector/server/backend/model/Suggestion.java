package com.bence.projector.server.backend.model;

import java.util.Date;
import java.util.List;

public class Suggestion extends BaseEntity {

    private String songId;
    private String title;
    private List<SongVerse> verses;
    private Date createdDate;
    private Boolean applied;
    private String createdByEmail;
    private String description;
    private String youtubeUrl;

    public Suggestion() {
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
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getApplied() {
        return applied;
    }

    public void setApplied(Boolean applied) {
        this.applied = applied;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
}
