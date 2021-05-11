package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SuggestionAssembler implements GeneralAssembler<Suggestion, SuggestionDTO> {
    private final SongVerseAssembler songVerseAssembler;
    private final SongService songService;

    @Autowired
    public SuggestionAssembler(SongVerseAssembler songVerseAssembler, SongService songService) {
        this.songVerseAssembler = songVerseAssembler;
        this.songService = songService;
    }

    @Override
    public SuggestionDTO createDto(Suggestion suggestion) {
        if (suggestion == null) {
            return null;
        }
        SuggestionDTO suggestionDTO = new SuggestionDTO();
        suggestionDTO.setUuid(suggestion.getUuid());
        suggestionDTO.setTitle(suggestion.getTitle());
        suggestionDTO.setCreatedDate(suggestion.getCreatedDate());
        suggestionDTO.setModifiedDate(suggestion.getModifiedDate());
        suggestionDTO.setVerses(songVerseAssembler.createDtoList(suggestion.getVerses()));
        suggestionDTO.setCreatedByEmail(suggestion.getCreatedByEmail());
        suggestionDTO.setApplied(suggestion.getApplied());
        suggestionDTO.setDescription(suggestion.getDescription());
        suggestionDTO.setSongId(suggestion.getSongUuid());
        suggestionDTO.setYoutubeUrl(suggestion.getYoutubeUrl());
        suggestionDTO.setReviewed(suggestion.getReviewed());
        User lastModifiedBy = suggestion.getLastModifiedBy();
        if (lastModifiedBy != null) {
            suggestionDTO.setLastModifiedByUserEmail(lastModifiedBy.getEmail());
        }
        return suggestionDTO;
    }

    @Override
    public Suggestion createModel(SuggestionDTO suggestionDTO) {
        final Suggestion suggestion = new Suggestion();
        suggestion.setCreatedDate(new Date());
        suggestion.setModifiedDate(suggestion.getCreatedDate());
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
        suggestion.setSong(songService.findOneByUuid(suggestionDTO.getSongId()));
        suggestion.setYoutubeUrl(suggestionDTO.getYoutubeUrl());
        suggestion.setReviewed(suggestionDTO.getReviewed());
        return suggestion;
    }
}
