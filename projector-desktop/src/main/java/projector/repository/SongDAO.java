package projector.repository;

import projector.model.Song;

import java.util.List;

public interface SongDAO extends CrudDAO<Song> {
    Song findByTitle(String title);

    List<Song> findAllByVersionGroup(String versionGroup);
}
