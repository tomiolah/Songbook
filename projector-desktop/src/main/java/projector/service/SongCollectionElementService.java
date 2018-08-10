package projector.service;

import projector.model.Song;
import projector.model.SongCollectionElement;

import java.util.List;

public interface SongCollectionElementService extends CrudService<SongCollectionElement> {
    List<SongCollectionElement> findBySong(Song song);
}
