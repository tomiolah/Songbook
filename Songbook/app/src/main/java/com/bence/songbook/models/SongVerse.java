package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

import static com.bence.songbook.ui.utils.StringUtils.stripAccents;

public class SongVerse extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @DatabaseField
    private String text;
    @DatabaseField
    private String strippedText;
    @DatabaseField
    private boolean isChorus;
    @DatabaseField(foreign = true, index = true)
    private Song song;

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
        strippedText = stripAccents(text.toLowerCase());
    }

    public boolean isChorus() {
        return isChorus;
    }

    public void setChorus(boolean chorus) {
        isChorus = chorus;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getStrippedText() {
        return strippedText;
    }
}
