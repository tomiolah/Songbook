package com.bence.projector.server.backend.model;

import com.bence.projector.common.model.SectionType;

public class SongVerse extends BaseEntity {
    private String text;
    private boolean isChorus;
    private String type;
    private SectionType sectionType = SectionType.VERSE;

    public SongVerse() {
    }

    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        this.isChorus = songVerse.isChorus;
        this.type = songVerse.type;
        this.sectionType = songVerse.sectionType;
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
        if (sectionType != null && sectionType == SectionType.CHORUS) {
            return true;
        }
        return isChorus;
    }

    public boolean matches(SongVerse songVerse) {
        return text.equals(songVerse.text) && isChorus == songVerse.isChorus;
    }

    public String getType() {
        return type;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }
}
