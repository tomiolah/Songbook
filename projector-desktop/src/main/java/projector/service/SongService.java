package projector.service;

import projector.model.Song;

import java.util.List;

public interface SongService extends CrudService<Song> {
    Song findByTitle(String title);

    List<Song> findAllByVersionGroup(String versionGroup);
}
