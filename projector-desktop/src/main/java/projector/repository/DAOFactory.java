package projector.repository;

import projector.repository.ormLite.OrmLiteDAOFactory;

public abstract class DAOFactory {

    public static DAOFactory getInstance() {
        return new OrmLiteDAOFactory();
    }

    public abstract SongBookDAO getSongBookDAO();

    public abstract SongDAO getSongDAO();

    public abstract InformationDAO getInformationDAO();

    public abstract SongVerseDAO getSongVerseDAO();

    public abstract LanguageRepository getLanguageDAO();

    public abstract SongCollectionRepository getSongCollectionDAO();

    public abstract SongCollectionElementRepository getSongCollectionElementDAO();
}
