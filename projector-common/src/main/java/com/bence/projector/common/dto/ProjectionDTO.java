package com.bence.projector.common.dto;

import java.util.List;

public class ProjectionDTO {
    private List<Long> verseIndices;

    public List<Long> getVerseIndices() {
        return verseIndices;
    }

    public void setVerseIndices(List<Long> verseIndices) {
        this.verseIndices = verseIndices;
    }
}
