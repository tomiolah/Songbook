package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongVerseDTO;
import com.bence.projector.server.backend.model.SongVerse;
import org.springframework.stereotype.Component;

@Component
public class SongVerseAssembler implements GeneralAssembler<SongVerse, SongVerseDTO> {
    @Override
    public SongVerseDTO createDto(SongVerse songVerse) {
        SongVerseDTO songVerseDTO = new SongVerseDTO();
        songVerseDTO.setText(songVerse.getText());
        songVerseDTO.setChorus(songVerse.isChorus());
        songVerseDTO.setType(songVerse.getType());
        return songVerseDTO;
    }

    @Override
    public SongVerse createModel(SongVerseDTO songVerseDTO) {
        return updateModel(new SongVerse(), songVerseDTO);
    }

    @Override
    public SongVerse updateModel(SongVerse songVerse, SongVerseDTO songVerseDTO) {
        songVerse.setText(songVerseDTO.getText());
        songVerse.setChorus(songVerseDTO.isChorus());
        songVerse.setType(songVerseDTO.getType());
        return songVerse;
    }
}
