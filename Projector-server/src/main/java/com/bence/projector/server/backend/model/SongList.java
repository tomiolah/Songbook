package com.bence.projector.server.backend.model;

import java.util.Date;
import java.util.List;

public class SongList extends BaseEntity {

    private String title;
    private String description;
    private Date createdDate;
    private Date modifiedDate;
    private List<SongListElement> songListElements;
    private String createdByEmail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List<SongListElement> getSongListElements() {
        return songListElements;
    }

    public void setSongListElements(List<SongListElement> songListElements) {
        this.songListElements = songListElements;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }
}
