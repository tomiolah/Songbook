package projector.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.repository.DAOFactory;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;
import projector.service.ServiceException;
import projector.service.SongService;

import java.util.List;

public class SongServiceImpl extends AbstractBaseService<Song> implements SongService {

    private final static Logger LOG = LoggerFactory.getLogger(SongServiceImpl.class);
    private SongDAO songDAO = DAOFactory.getInstance().getSongDAO();

    public SongServiceImpl() {
        super(DAOFactory.getInstance().getSongDAO());
    }

    @Override
    public List<Song> findAll() throws ServiceException {
        try {
            return songDAO.findAll();
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song create(Song song) throws ServiceException {
        try {
            return songDAO.create(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song update(Song song) throws ServiceException {
        try {
            return songDAO.create(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
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
            return songDAO.findByTitle(title);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        try {
            return songDAO.findAllByVersionGroup(versionGroup);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}
