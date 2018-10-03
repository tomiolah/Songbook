package com.bence.songbook.models;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class FavouriteSong extends Base {

    @Expose
    @DatabaseField(columnName = "song_id", foreign = true, foreignAutoRefresh = true, index = true, unique = true)
    private Song song;
    @Expose
    @DatabaseField
    private boolean favourite;
    @DatabaseField
    private boolean favouritePublished = true;
    @DatabaseField
    private boolean favouritePublishedToDrive = false;
    @Expose
    @DatabaseField
    private Date modifiedDate;

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isFavouritePublished() {
        return favouritePublished;
    }

    public void setFavouritePublished(boolean favouritePublished) {
        this.favouritePublished = favouritePublished;
    }

    public boolean isFavouritePublishedToDrive() {
        return favouritePublishedToDrive;
    }

    public void setFavouritePublishedToDrive(boolean favouritePublishedToDrive) {
        this.favouritePublishedToDrive = favouritePublishedToDrive;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
