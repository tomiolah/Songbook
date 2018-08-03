package com.bence.projector.server.backend.model;

import java.util.List;

public class BibleVerse {

    private String text;
    private List<Long> verseIndices;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public List<Long> getVerseIndices() {
        return verseIndices;
    }

    public void setVerseIndices(List<Long> verseIndices) {
        this.verseIndices = verseIndices;
    }
}
