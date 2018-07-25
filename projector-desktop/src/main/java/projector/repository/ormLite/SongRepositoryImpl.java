package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;

import java.sql.SQLException;
import java.util.List;

public class SongRepositoryImpl extends AbstractRepository<Song> implements SongDAO {
    private static final Logger LOG = LoggerFactory.getLogger(SongRepositoryImpl.class);

    private SongVerseRepositoryImpl songVerseRepository;

    SongRepositoryImpl() throws SQLException {
        super(Song.class, DatabaseHelper.getInstance().getSongDao());
        songVerseRepository = new SongVerseRepositoryImpl();
    }

    @Override
    public Song create(Song song) throws RepositoryException {
        Long id = song.getId();
        if (id != null) {
            Song byId = findById(id);
            if (byId != null) {
                songVerseRepository.deleteAll(byId.getVerses());
            }
        }
        final Song song1 = super.create(song);
        songVerseRepository.create(song.getVerses());
        return song1;
    }

    @Override
    public List<Song> create(List<Song> songs) throws RepositoryException {
        List<Song> songList = super.create(songs);
        for (Song song : songList) {
            songVerseRepository.create(song.getVerses());
        }
        return songList;
    }

    @Override
    public boolean delete(Song song) throws RepositoryException {
        songVerseRepository.deleteAll(song.getVerses());
        return super.delete(song);
    }

    @Override
    public boolean deleteAll(List<Song> songs) throws RepositoryException {
        for (Song song : songs) {
            delete(song);
        }
        return true;
    }

    @Override
    public Song findByTitle(String title) {
        String msg = "Could not find song";
        try {
            List<Song> songs = dao.queryForEq("title", title);
            if (songs != null && songs.size() > 0) {
                return songs.get(0);
            }
            return null;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        String msg = "Could not find song versions";
        try {
            List<Song> songs = dao.queryForEq("versionGroup", versionGroup);
            Song byUUID = findByUuid(versionGroup);
            if (byUUID != null) {
                songs.add(byUUID);
            }
            return songs;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }
}
