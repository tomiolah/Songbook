package com.bence.projector.server.backend.model;

public class SongVerse extends BaseEntity {
    private String text;
    private boolean isChorus;

    public SongVerse() {
    }

    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        this.isChorus = songVerse.isChorus;
    }

    public static SongVerse[] cloneList(SongVerse[] songVerses) {
        SongVerse[] clonedSongVerses = new SongVerse[songVerses.length];
        for (int i = 0; i < songVerses.length; ++i) {
            clonedSongVerses[i] = new SongVerse(songVerses[i]);
        }
        return clonedSongVerses;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChorus() {
        return isChorus;
    }

    public void setChorus(boolean chorus) {
        isChorus = chorus;
    }
}
