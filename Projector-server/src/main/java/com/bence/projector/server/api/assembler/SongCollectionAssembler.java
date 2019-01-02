package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SongCollectionAssembler implements GeneralAssembler<SongCollection, SongCollectionDTO> {

    @Override
    public SongCollectionDTO createDto(SongCollection songCollection) {
        if (songCollection == null) {
            return null;
        }
        SongCollectionDTO songCollectionDTO = new SongCollectionDTO();
        songCollectionDTO.setUuid(songCollection.getId());
        songCollectionDTO.setCreatedDate(songCollection.getCreatedDate());
        songCollectionDTO.setModifiedDate(songCollection.getModifiedDate());
        ArrayList<SongCollectionElementDTO> songCollectionElements = new ArrayList<>();
        for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
            songCollectionElements.add(createElementModelDTO(songCollectionElement));
        }
        songCollectionDTO.setSongCollectionElements(songCollectionElements);
        songCollectionDTO.setName(songCollection.getName());
        songCollectionDTO.setLanguageUuid(songCollection.getId());
        return songCollectionDTO;
    }

    private SongCollectionElementDTO createElementModelDTO(SongCollectionElement songCollectionElement) {
        SongCollectionElementDTO songCollectionElementDTO = new SongCollectionElementDTO();
        songCollectionElementDTO.setOrdinalNumber(songCollectionElement.getOrdinalNumber());
        songCollectionElementDTO.setSongUuid(songCollectionElement.getSongUuid());
        return songCollectionElementDTO;
    }

    public SongCollectionElement createElementModel(SongCollectionElementDTO songCollectionElementDTO) {
        SongCollectionElement songCollectionElement = new SongCollectionElement();
        songCollectionElement.setOrdinalNumber(songCollectionElementDTO.getOrdinalNumber());
        songCollectionElement.setSongUuid(songCollectionElementDTO.getSongUuid());
        return songCollectionElement;
    }

    @Override
    public SongCollection createModel(SongCollectionDTO songCollectionDTO) {
        final SongCollection songCollection = new SongCollection();
        songCollection.setId(songCollectionDTO.getUuid());
        return updateModel(songCollection, songCollectionDTO);
    }

    @Override
    public SongCollection updateModel(SongCollection songCollection, SongCollectionDTO songCollectionDTO) {
        return songCollection;
    }
}
