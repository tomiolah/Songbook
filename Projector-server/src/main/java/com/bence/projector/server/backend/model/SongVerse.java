package com.bence.projector.server.backend.model;

import com.bence.projector.common.model.SectionType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class SongVerse extends BaseEntity {
    private static final int MAX_TEXT_LENGTH = 1000;
    @Column(length = MAX_TEXT_LENGTH)
    private String text;
    private String type;
    private SectionType sectionType = SectionType.VERSE;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;
    @ManyToOne(fetch = FetchType.LAZY)
    private Suggestion suggestion;

    public SongVerse() {
    }

    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        this.type = songVerse.type;
        this.sectionType = songVerse.sectionType;
        this.song = songVerse.song;
        this.suggestion = songVerse.suggestion;
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
        this.text = text.substring(0, Math.min(text.length(), MAX_TEXT_LENGTH));
    }

    public boolean isChorus() {
        return sectionType != null && sectionType == SectionType.CHORUS;
    }

    public boolean matches(SongVerse songVerse) {
        return text.equals(songVerse.text) && isChorus() == songVerse.isChorus();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SectionType getSectionType() {
        if (isChorus()) {
            sectionType = SectionType.CHORUS;
        }
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
    }
}
