package projector.service.impl;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.repository.DAOFactory;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;
import projector.service.ServiceException;
import projector.service.SongService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SongServiceImpl extends AbstractBaseService<Song> implements SongService {

    private final static Logger LOG = LoggerFactory.getLogger(SongServiceImpl.class);
    private final SongDAO songDAO = DAOFactory.getInstance().getSongDAO();
    private final HashMap<Long, Song> hashMap = new HashMap<>();

    public SongServiceImpl() {
        super(DAOFactory.getInstance().getSongDAO());
    }

    @Override
    public Song findByUuid(String uuid) {
        Song song = super.findByUuid(uuid);
        return getSongFromHashMap(song);
    }

    private Song getSongFromHashMap(Song song) {
        if (song == null) {
            return null;
        }
        Long id = song.getId();
        if (hashMap.containsKey(id)) {
            return hashMap.get(id);
        } else {
            hashMap.put(id, song);
        }
        return song;
    }

    @Override
    public Song findById(Long id) {
        Song song = super.findById(id);
        return getSongFromHashMap(song);
    }

    @Override
    public List<Song> findAll() throws ServiceException {
        try {
            List<Song> songList = songDAO.findAll();
            return getSongsFromHashMap(songList);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private List<Song> getSongsFromHashMap(List<Song> songList) {
        List<Song> songs = new ArrayList<>(songList.size());
        for (Song song : songList) {
            songs.add(getSongFromHashMap(song));
        }
        return songs;
    }

    @Override
    public Song create(Song song) throws ServiceException {
        return updateSong(song);
    }

    private Song updateSong(Song song) {
        try {
            Song updatedSong = songDAO.create(song);
            if (updatedSong != null) {
                hashMap.put(updatedSong.getId(), updatedSong);
            }
            return updatedSong;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song update(Song song) throws ServiceException {
        return updateSong(song);
    }

    @Override
    public boolean delete(Song song) throws ServiceException {
        try {
            return songDAO.delete(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(List<Song> songs) throws ServiceException {
        try {
            return songDAO.deleteAll(songs);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song findByTitle(String title) {
        try {
            Song song = songDAO.findByTitle(title);
            return getSongFromHashMap(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        try {
            List<Song> songs = songDAO.findAllByVersionGroup(versionGroup);
            return getSongsFromHashMap(songs);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveViews(List<SongViewsDTO> songViewsDTOS) {
        try {
            songDAO.saveViews(songViewsDTOS);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS) {
        try {
            songDAO.saveFavouriteCount(songFavouritesDTOS);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song getFromMemoryOrSong(Song song) {
        return getSongFromHashMap(song);
    }
}
