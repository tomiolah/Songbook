package com.bence.projector.server.backend.model;

import java.util.List;

public class Chapter {
    private List<BibleVerse> verses;

    public List<BibleVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<BibleVerse> verses) {
        this.verses = verses;
    }

}