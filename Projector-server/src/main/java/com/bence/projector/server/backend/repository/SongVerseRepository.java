package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongVerse;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface SongVerseRepository extends CrudRepository<SongVerse, Long> {
    @Transactional
    void deleteAllBySongId(Long songId);
}
