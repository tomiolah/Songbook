package com.bence.projector.common.dto;

import java.util.Date;

public class FavouriteSongDTO extends BaseDTO {

    private String userUuid;
    private String songUuid;
    private Date modifiedDate;
    private boolean favourite;
    private Date serverModifiedDate;

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public Date getServerModifiedDate() {
        return serverModifiedDate;
    }

    public void setServerModifiedDate(Date serverModifiedDate) {
        this.serverModifiedDate = serverModifiedDate;
    }
}
