package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.SongList;
import com.bence.projector.server.backend.repository.SongListRepository;
import com.bence.projector.server.backend.service.SongListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SongListServiceImpl extends BaseServiceImpl<SongList> implements SongListService {

    @Autowired
    private SongListRepository songListRepository;

    @Override
    public SongList findOneByUuid(String uuid) {
        return songListRepository.findOneByUuid(uuid);
    }
}
