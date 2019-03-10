package com.bence.projector.server.backend.model;

public class SongCollectionElement {

    private String ordinalNumber;
    private String songUuid;

    public String getOrdinalNumber() {
        return ordinalNumber;
    }

    public void setOrdinalNumber(String ordinalNumber) {
        this.ordinalNumber = ordinalNumber;
    }

    @Override
    public String toString() {
        return ordinalNumber;
    }

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public boolean matches(SongCollectionElement songCollectionElement) {
        if (songCollectionElement == null) {
            return false;
        }
        if (!ordinalNumber.equals(songCollectionElement.ordinalNumber)) {
            return false;
        }
        return songUuid.equals(songCollectionElement.songUuid);
    }
}
