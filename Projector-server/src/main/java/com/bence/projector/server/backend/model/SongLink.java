package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import com.bence.projector.server.backend.service.SongService;

import java.util.Date;

@Entity
public class SongLink extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song1;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song2;
    private Date createdDate;
    private Date modifiedDate;
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

    public Date getModifiedDate() {
        if (modifiedDate == null) {
            return getCreatedDate();
        }
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isUnApplied() {
        Boolean applied = getApplied();
        if (applied == null) {
            return true;
        }
        return !applied;
    }

    public Song getSong1(SongService songService) {
        return songService.findOne(songId1);
    }

    public Song getSong2(SongService songService) {
        return songService.findOne(songId2);
    }

    public boolean hasLanguage(Language language, SongService songService) {
        if (language == null) {
            return false;
        }
        Song song1 = getSong1(songService);
        if (song1 != null && language.equals(song1.getLanguage())) {
            return true;
        }
        Song song2 = getSong2(songService);
        return song2 != null && language.equals(song2.getLanguage());
    }

    public boolean alreadyTheSameVersionGroup(SongService songService) {
        Song song1 = getSong1(songService);
        if (song1 == null) {
            return false;
        }
        Song song2 = getSong2(songService);
        return song1.isSameVersionGroup(song2);
    }
}
