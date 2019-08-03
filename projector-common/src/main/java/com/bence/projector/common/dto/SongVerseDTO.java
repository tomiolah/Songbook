package com.bence.projector.common.dto;

public class SongVerseDTO {

    private String text;
    private Boolean chorus = false;
    private int type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean isChorus() {
        return chorus;
    }

    public void setChorus(Boolean chorus) {
        this.chorus = chorus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
