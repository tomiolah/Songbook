package com.bence.projector.server.backend.model;

import java.util.Date;

public class SongLink extends BaseEntity {

    private String songId1;
    private String songId2;
    private Date createdDate;
    private Boolean applied;
    private String createdByEmail;

    public String getSongId1() {
        return songId1;
    }

    public void setSongId1(String songId1) {
        this.songId1 = songId1;
    }

    public String getSongId2() {
        return songId2;
    }

    public void setSongId2(String songId2) {
        this.songId2 = songId2;
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
}
