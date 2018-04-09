package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SongAssembler implements GeneralAssembler<Song, SongDTO> {
    @Autowired
    private SongVerseAssembler songVerseAssembler;
    @Autowired
    private LanguageAssembler languageAssembler;
    @Autowired
    private LanguageRepository languageRepository;

    @Override
    public SongDTO createDto(Song song) {
        if (song == null) {
            return null;
        }
        SongDTO songDTO = new SongDTO();
        songDTO.setUuid(song.getId());
        songDTO.setOriginalId(song.getOriginalId());
        songDTO.setTitle(song.getTitle());
        songDTO.setCreatedDate(song.getCreatedDate());
        songDTO.setModifiedDate(song.getModifiedDate());
        songDTO.setSongVerseDTOS(songVerseAssembler.createDtoList(song.getVerses()));
        songDTO.setDeleted(song.isDeleted());
        songDTO.setLanguageDTO(languageAssembler.createDto(song.getLanguage()));
        songDTO.setUploaded(song.getUploaded());
        songDTO.setViews(song.getViews());
        songDTO.setCreatedByEmail(song.getCreatedByEmail());
        return songDTO;
    }

    @Override
    public Song createModel(SongDTO songDTO) {
        final Song song = new Song();
        Date createdDate = songDTO.getCreatedDate();
        if (createdDate == null || createdDate.getTime() < 1000) {
            song.setCreatedDate(new Date());
        } else {
            song.setCreatedDate(createdDate);
        }
        return updateModel(song, songDTO);
    }

    @Override
    public Song updateModel(Song song, SongDTO songDTO) {
        if (song.getId() != null && song.getId().equals(songDTO.getOriginalId())) {
            song.setOriginalId(null);
        } else {
            song.setOriginalId(songDTO.getOriginalId());
        }
        song.setTitle(songDTO.getTitle());
        Date modifiedDate = songDTO.getModifiedDate();
        if (modifiedDate == null || modifiedDate.getTime() < 1000) {
            song.setModifiedDate(new Date());
        } else {
            song.setModifiedDate(modifiedDate);
        }
        final List<SongVerse> songVerses = songVerseAssembler.createModelList(songDTO.getSongVerseDTOS());
        song.setVerses(songVerses);
        song.setDeleted(songDTO.isDeleted());
        if (!songDTO.isDeleted() && songDTO.getLanguageDTO() != null) {
            song.setLanguage(languageRepository.findOne(songDTO.getLanguageDTO().getUuid()));
        }
        song.setCreatedByEmail(songDTO.getCreatedByEmail());
        return song;
    }
}
