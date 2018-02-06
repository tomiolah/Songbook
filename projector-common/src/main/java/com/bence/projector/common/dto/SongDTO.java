package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class SongDTO extends BaseDTO {

    private String title;
    private Date createdDate;
    private Date modifiedDate;
    private List<SongVerseDTO> songVerseDTOS;
    private boolean deleted = false;
    private LanguageDTO languageDTO;
    private Boolean uploaded;

    public SongDTO() {
    }

    public SongDTO(SongDTO songDTO) {
        setTitle(songDTO.getTitle());
        setCreatedDate(songDTO.getCreatedDate());
        setDeleted(songDTO.isDeleted());
        setModifiedDate(songDTO.getModifiedDate());
        setSongVerseDTOS(songDTO.getSongVerseDTOS());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SongVerseDTO> getSongVerseDTOS() {
        return songVerseDTOS;
    }

    public void setSongVerseDTOS(List<SongVerseDTO> songVerseDTOS) {
        this.songVerseDTOS = songVerseDTOS;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LanguageDTO getLanguageDTO() {
        return languageDTO;
    }

    public void setLanguageDTO(LanguageDTO languageDTO) {
        this.languageDTO = languageDTO;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }
}
