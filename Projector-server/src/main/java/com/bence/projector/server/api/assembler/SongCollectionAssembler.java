package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

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
        return updateModel(songCollection, songCollectionDTO);
    }

    @Override
    public SongCollection updateModel(SongCollection songCollection, SongCollectionDTO songCollectionDTO) {
        songCollection.setCreatedDate(songCollectionDTO.getCreatedDate());
        songCollection.setModifiedDate(new Date());
        songCollection.setName(songCollectionDTO.getName());
        ArrayList<SongCollectionElement> songCollectionElements = new ArrayList<>();
        for (SongCollectionElementDTO dto : songCollectionDTO.getSongCollectionElements()) {
            SongCollectionElement songCollectionElement = new SongCollectionElement();
            songCollectionElement.setOrdinalNumber(dto.getOrdinalNumber());
            songCollectionElement.setSongUuid(dto.getSongUuid());
            songCollectionElements.add(songCollectionElement);
        }
        songCollection.setSongCollectionElements(songCollectionElements);
        return songCollection;
    }
}
