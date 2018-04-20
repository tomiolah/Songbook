package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.projector.server.backend.model.SongLink;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SongLinkAssembler implements GeneralAssembler<SongLink, SongLinkDTO> {

    @Override
    public SongLinkDTO createDto(SongLink songLink) {
        if (songLink == null) {
            return null;
        }
        SongLinkDTO songLinkDTO = new SongLinkDTO();
        songLinkDTO.setUuid(songLink.getId());
        songLinkDTO.setCreatedDate(songLink.getCreatedDate());
        songLinkDTO.setCreatedByEmail(songLink.getCreatedByEmail());
        songLinkDTO.setApplied(songLink.getApplied());
        songLinkDTO.setSongId1(songLink.getSongId1());
        songLinkDTO.setSongId2(songLink.getSongId2());
        return songLinkDTO;
    }

    @Override
    public SongLink createModel(SongLinkDTO songLinkDTO) {
        final SongLink songLink = new SongLink();
        songLink.setCreatedDate(new Date());
        return updateModel(songLink, songLinkDTO);
    }

    @Override
    public SongLink updateModel(SongLink songLink, SongLinkDTO songLinkDTO) {
        songLink.setCreatedByEmail(songLinkDTO.getCreatedByEmail());
        songLink.setApplied(songLinkDTO.getApplied());
        songLink.setSongId1(songLinkDTO.getSongId1());
        songLink.setSongId2(songLinkDTO.getSongId2());
        return songLink;
    }
}
