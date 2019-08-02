package com.bence.projector.common.dto;

public class SongVerseDTO {

    private String text;
    private Boolean chorus = false;
    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
