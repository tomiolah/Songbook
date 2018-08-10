package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.model.SongCollectionElement;
import projector.repository.RepositoryException;
import projector.repository.SongCollectionElementRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class SongCollectionElementRepositoryImpl extends AbstractBaseRepository<SongCollectionElement> implements SongCollectionElementRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SongCollectionElementRepositoryImpl.class);

    SongCollectionElementRepositoryImpl() throws SQLException {
        super(SongCollectionElement.class, DatabaseHelper.getInstance().getSongCollectionElementDao());
    }

    @Override
    public List<SongCollectionElement> findBySong(Song song) {
        String msg = "Could not find song";
        try {
            if (song == null || song.getUuid() == null) {
                return new ArrayList<>();
            }
            return dao.queryForEq("SONGUUID", song.getUuid());
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }
}
