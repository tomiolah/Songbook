package com.bence.songbook.api.assembler;

import com.bence.projector.common.dto.SongDTO;
import com.bence.songbook.ProgressMessage;
import com.bence.songbook.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAssembler implements GeneralAssembler<Song, SongDTO> {

    private static SongAssembler instance;
    private static SongVerseAssembler songVerseAssembler = SongVerseAssembler.getInstance();

    private SongAssembler() {
    }

    public static SongAssembler getInstance() {
        if (instance == null) {
            instance = new SongAssembler();
        }
        return instance;
    }

    @Override
    public synchronized Song createModel(SongDTO songDTO) {
        return updateModel(new Song(), songDTO);
    }

    @Override
    public synchronized Song updateModel(Song song, SongDTO songDTO) {
        if (song != null) {
            song.setUuid(songDTO.getUuid());
            song.setTitle(songDTO.getTitle());
            song.setCreatedDate(songDTO.getCreatedDate());
            song.setModifiedDate(songDTO.getModifiedDate());
            song.setVerses(songVerseAssembler.createModelList(songDTO.getSongVerseDTOS()));
            song.setDeleted(songDTO.isDeleted());
        }
        return song;
    }

    @Override
    public synchronized List<Song> createModelList(List<SongDTO> ds) {
        if (ds == null) {
            return null;
        }
        List<Song> models = new ArrayList<>();
        for (SongDTO songDTO : ds) {
            models.add(createModel(songDTO));
        }
        return models;
    }

    public synchronized List<Song> createModelList(List<SongDTO> ds, ProgressMessage progressMessage) {
        if (ds == null) {
            return null;
        }
        List<Song> models = new ArrayList<>();
        int i = 0;
        for (SongDTO songDTO : ds) {
            models.add(createModel(songDTO));
            progressMessage.onProgress(++i);
        }
        return models;
    }
}
