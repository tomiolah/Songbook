package com.bence.projector.common.dto;

import com.google.gson.annotations.Expose;

public class SongVerseProjectionDTO {

    @Expose
    private String wholeWithFocusedText;
    @Expose
    private String focusedText;
    @Expose
    private Integer focusedTextIndex;
    @Expose
    private boolean textsSplit;
    @Expose
    private Integer songVerseIndex;
    @Expose
    private boolean lastOne;


    public String getFocusedText() {
        return focusedText;
    }

    public void setFocusedText(String focusedText) {
        this.focusedText = focusedText;
    }

    public String getWholeWithFocusedText() {
        return wholeWithFocusedText;
    }

    public void setWholeWithFocusedText(String wholeWithFocusedText) {
        this.wholeWithFocusedText = wholeWithFocusedText;
    }

    public void setFocusedTextIndex(Integer focusedTextIndex) {
        this.focusedTextIndex = focusedTextIndex;
    }

    public Integer getFocusedTextIndex() {
        return focusedTextIndex;
    }

    public void setTextsSplit(boolean textsSplit) {
        this.textsSplit = textsSplit;
    }

    public boolean isTextsSplit() {
        return textsSplit;
    }

    public void setSongVerseIndex(Integer songVerseIndex) {
        this.songVerseIndex = songVerseIndex;
    }

    public Integer getSongVerseIndex() {
        return songVerseIndex;
    }

    public void setLastOne(boolean lastOne) {
        this.lastOne = lastOne;
    }

    public boolean isLastOne() {
        return lastOne;
    }
}
