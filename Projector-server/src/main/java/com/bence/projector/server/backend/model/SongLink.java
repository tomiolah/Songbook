package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class SongLink extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song1;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song2;
    private Date createdDate;
    private Boolean applied;
    private String createdByEmail;

    public void setSong1(Song song1) {
        this.song1 = song1;
    }

    public void setSong2(Song song2) {
        this.song2 = song2;
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
