package projector.repository.ormLite;

import projector.model.SongCollectionElement;
import projector.repository.SongCollectionElementRepository;

import java.sql.SQLException;

class SongCollectionElementRepositoryImpl extends AbstractRepository<SongCollectionElement> implements SongCollectionElementRepository {

    SongCollectionElementRepositoryImpl() throws SQLException {
        super(SongCollectionElement.class, DatabaseHelper.getInstance().getSongCollectionElementDao());
    }
}
