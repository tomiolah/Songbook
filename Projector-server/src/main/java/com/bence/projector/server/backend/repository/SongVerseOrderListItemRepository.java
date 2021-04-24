package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerseOrderListItem;
import org.springframework.data.repository.CrudRepository;

public interface SongVerseOrderListItemRepository extends CrudRepository<SongVerseOrderListItem, Long> {
    void deleteBySong(Song song);
}
