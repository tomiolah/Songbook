package com.bence.projector.common.dto;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ProjectionDTO {

    @Expose
    private List<Long> verseIndices;

    public List<Long> getVerseIndices() {
        return verseIndices;
    }

    public void setVerseIndices(List<Long> verseIndices) {
        this.verseIndices = verseIndices;
    }
}
