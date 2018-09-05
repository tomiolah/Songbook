package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.Suggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SuggestionAssembler implements GeneralAssembler<Suggestion, SuggestionDTO> {
    private final SongVerseAssembler songVerseAssembler;

    @Autowired
    public SuggestionAssembler(SongVerseAssembler songVerseAssembler) {
        this.songVerseAssembler = songVerseAssembler;
    }

    @Override
    public SuggestionDTO createDto(Suggestion suggestion) {
        if (suggestion == null) {
            return null;
        }
        SuggestionDTO suggestionDTO = new SuggestionDTO();
        suggestionDTO.setUuid(suggestion.getId());
        suggestionDTO.setTitle(suggestion.getTitle());
        suggestionDTO.setCreatedDate(suggestion.getCreatedDate());
        suggestionDTO.setVerses(songVerseAssembler.createDtoList(suggestion.getVerses()));
        suggestionDTO.setCreatedByEmail(suggestion.getCreatedByEmail());
        suggestionDTO.setApplied(suggestion.getApplied());
        suggestionDTO.setDescription(suggestion.getDescription());
        suggestionDTO.setSongId(suggestion.getSongId());
        suggestionDTO.setYoutubeUrl(suggestion.getYoutubeUrl());
        return suggestionDTO;
    }

    @Override
    public Suggestion createModel(SuggestionDTO suggestionDTO) {
        final Suggestion suggestion = new Suggestion();
        suggestion.setCreatedDate(new Date());
        return updateModel(suggestion, suggestionDTO);
    }

    @Override
    public Suggestion updateModel(Suggestion suggestion, SuggestionDTO suggestionDTO) {
        suggestion.setTitle(suggestionDTO.getTitle());
        final List<SongVerse> songVerses = songVerseAssembler.createModelList(suggestionDTO.getVerses());
        suggestion.setVerses(songVerses);
        suggestion.setCreatedByEmail(suggestionDTO.getCreatedByEmail());
        suggestion.setApplied(suggestionDTO.getApplied());
        suggestion.setDescription(suggestionDTO.getDescription());
        suggestion.setSongId(suggestionDTO.getSongId());
        suggestion.setYoutubeUrl(suggestionDTO.getYoutubeUrl());
        return suggestion;
    }
}
